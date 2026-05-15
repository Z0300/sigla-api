package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.UserRequest;
import com.api.springcore.entity.Role;
import com.api.springcore.entity.User;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.repository.RoleRepository;
import com.api.springcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<DomainResponse.UserSummaryDto> getUsers(String search, Pageable pageable) {
        return userRepository.findAllWithSearch(search, pageable)
                .map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public DomainResponse.UserDto getUser(Long id) {
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDto(user);
    }

    @Transactional
    public DomainResponse.UserDto createUser(UserRequest.Create request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Set<Role> roles = resolveRoles(request.getRoleIds());

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .isEmailVerified(false)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("Admin created user: {}", user.getEmail());
        return toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto updateUser(Long id, UserRequest.Update request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName()))  user.setLastName(request.getLastName());
        if (request.getIsActive() != null)               user.setIsActive(request.getIsActive());

        user = userRepository.save(user);
        return toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    @Transactional
    public DomainResponse.UserDto assignRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> roles = resolveRoles(request.getRoleIds());
        user.setRoles(roles);
        user = userRepository.save(user);
        return toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto addRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> newRoles = resolveRoles(request.getRoleIds());
        newRoles.forEach(user::addRole);
        user = userRepository.save(user);
        return toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto removeRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> rolesToRemove = resolveRoles(request.getRoleIds());
        rolesToRemove.forEach(user::removeRole);
        user = userRepository.save(user);
        return toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));
            return new HashSet<>(Set.of(defaultRole));
        }
        List<Role> found = roleRepository.findAllByIdIn(roleIds);
        if (found.size() != roleIds.size()) {
            throw new BadRequestException("One or more role IDs are invalid");
        }
        return new HashSet<>(found);
    }

    private DomainResponse.UserDto toDto(User user) {
        return DomainResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(user.getRoles().stream().map(this::toRoleDto).collect(Collectors.toSet()))
                .build();
    }

    private DomainResponse.UserSummaryDto toSummaryDto(User user) {
        return DomainResponse.UserSummaryDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private DomainResponse.RoleDto toRoleDto(Role role) {
        return DomainResponse.RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(p -> DomainResponse.PermissionDto.builder()
                                .id(p.getId()).name(p.getName()).description(p.getDescription()).build())
                        .collect(Collectors.toSet()))
                .build();
    }
}