package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.UserRequest;
import com.api.springcore.entity.Role;
import com.api.springcore.entity.User;
import com.api.springcore.exception.*;
import com.api.springcore.helper.RoleResolver;
import com.api.springcore.mapper.UserMapper;
import com.api.springcore.repository.UserRepository;
import com.api.springcore.security.CustomUserDetailsService.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper      userMapper;
    private final RoleResolver    roleResolver;


    private static final List<String> PROTECTED_ROLES = List.of("SUPER_ADMIN", "ADMIN");

    @Transactional(readOnly = true)
    public Page<DomainResponse.UserSummaryDto> getUsers(String searchTerm, Pageable pageable) {
        Page<Long> idPage = userRepository.findIdsBySearch(searchTerm, pageable);

        if (idPage.isEmpty()) return Page.empty(pageable);

        List<User> users = userRepository.findAllByIds(idPage.getContent());

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<DomainResponse.UserSummaryDto> summaryDtoList = idPage.getContent().stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(userMapper::toSummaryDto)
                .toList();

        return new PageImpl<>(summaryDtoList, pageable, idPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DomainResponse.UserDto getUser(Long id) {
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toDto(user);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Transactional
    public DomainResponse.UserDto createUser(UserRequest.Create request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Set<Role> roles = roleResolver.resolve(request.getRoleIds());


        if (currentUserLacksSuperAdminRole()) {
            boolean assigningProtectedRole = roles.stream()
                    .anyMatch(r -> PROTECTED_ROLES.contains(r.getName()));
            if (assigningProtectedRole) {
                throw new ForbiddenException("Only SUPER_ADMIN can create ADMIN or SUPER_ADMIN accounts");
            }
        }

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
        return userMapper.toDto(
                userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto updateUser(Long id, UserRequest.Update request) {
        UserPrincipal currentUser = getCurrentUser();

        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (id.equals(currentUser.id()) && Boolean.FALSE.equals(request.getIsActive())) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        if (!currentUserLacksSuperAdminRole()) {
            guardProtectedUser(user, "manage");
        }

        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName()))  user.setLastName(request.getLastName());
        if (request.getIsActive() != null)               user.setIsActive(request.getIsActive());

        user = userRepository.save(user);
        return userMapper.toDto(
                userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public void deleteUser(Long id) {
        UserPrincipal currentUser = getCurrentUser();

        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (id.equals(currentUser.id())) {
            throw new BadRequestException("You cannot delete your own account");
        }

        if (!currentUserLacksSuperAdminRole()) {
            guardProtectedUser(user, "delete");
        }

        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    @Transactional
    public DomainResponse.UserDto assignRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> roles = roleResolver.resolve(request.getRoleIds());

        if (!currentUserLacksSuperAdminRole()) {
            guardRoleAssignment(roles);
        }

        user.setRoles(roles);
        user = userRepository.save(user);
        return userMapper.toDto(
                userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto addRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> newRoles = roleResolver.resolve(request.getRoleIds());

        if (!currentUserLacksSuperAdminRole()) {
            guardRoleAssignment(newRoles);
        }

        newRoles.forEach(user::addRole);
        user = userRepository.save(user);
        return userMapper.toDto(
                userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto removeRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> rolesToRemove = roleResolver.resolve(request.getRoleIds());

        if (!currentUserLacksSuperAdminRole()) {
            boolean removingSuperAdmin = rolesToRemove.stream()
                    .anyMatch(r -> "SUPER_ADMIN".equals(r.getName()));
            if (removingSuperAdmin) {
                throw new ForbiddenException("Only SUPER_ADMIN can remove the SUPER_ADMIN role");
            }
        }

        rolesToRemove.forEach(user::removeRole);
        user = userRepository.save(user);
        return userMapper.toDto(
                userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return principal;
    }

    private boolean currentUserLacksSuperAdminRole() {
        return getCurrentUser().getAuthorities().stream()
                .noneMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    private void guardProtectedUser(User target, String action) {
        boolean targetIsProtected = target.getRoles().stream()
                .anyMatch(r -> PROTECTED_ROLES.contains(r.getName()));
        if (targetIsProtected) {
            throw new ForbiddenException(
                    "You do not have permission to " + action + " ADMIN or SUPER_ADMIN accounts");
        }
    }

    private void guardRoleAssignment(Set<Role> roles) {
        boolean assigningProtected = roles.stream()
                .anyMatch(r -> PROTECTED_ROLES.contains(r.getName()));
        if (assigningProtected) {
            throw new ForbiddenException("Only SUPER_ADMIN can assign ADMIN or SUPER_ADMIN roles");
        }
    }
}