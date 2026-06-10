package com.api.springcore.service;

import com.api.springcore.dto.AuthRequest;
import com.api.springcore.dto.DomainResponse;
import com.api.springcore.entity.Permission;
import com.api.springcore.entity.RefreshToken;
import com.api.springcore.entity.Role;
import com.api.springcore.entity.User;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.exception.UnauthorizedException;
import com.api.springcore.helper.TokenResolver;
import com.api.springcore.repository.PermissionRepository;
import com.api.springcore.repository.RefreshTokenRepository;
import com.api.springcore.repository.RoleRepository;
import com.api.springcore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final PermissionRepository permissionRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final TokenResolver tokenResolver;

    @Transactional
    public DomainResponse.TokenDto register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));

        Permission viewEvents = permissionRepository.findByName("events:read")
                .orElseThrow(() -> new ResourceNotFoundException("Permission events:read not found"));

        userRole.addPermission(viewEvents);

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isEmailVerified(false)
                .build();
        user.addRole(userRole);

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());
        return tokenResolver.buildTokenResponse(user);
    }

    @Transactional
    public DomainResponse.TokenDto login(AuthRequest.Login request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailWithRolesAndPermissions(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        return tokenResolver.buildTokenResponse(user);
    }

    @Transactional
    public DomainResponse.TokenDto refreshToken(String rawRefreshToken) {
        String tokenHash = tokenResolver.hashToken(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!stored.isValid()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findByIdWithRolesAndPermissions(stored.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", stored.getUser().getId()));


        return tokenResolver.buildTokenResponse(user);
    }

    @Transactional
    public void logout(Long userId) {
        int revoked = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh tokens for user {}", revoked, userId);
    }

    @Transactional
    public void changePassword(Long userId, AuthRequest.ChangePassword request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from your current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Password changed for user {}", userId);
    }

}