package com.api.springcore.helper;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.entity.RefreshToken;
import com.api.springcore.entity.Role;
import com.api.springcore.entity.User;
import com.api.springcore.repository.RefreshTokenRepository;
import com.api.springcore.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TokenResolver {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public DomainResponse.TokenDto buildTokenResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getAllPermissions();

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles, permissions);
        String rawRefresh  = UUID.randomUUID().toString();
        String refreshHash = hashToken(rawRefresh);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        DomainResponse.UserSummaryDto userSummary = DomainResponse.UserSummaryDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return DomainResponse.TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(userSummary)
                .build();
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}