package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.UserRequest;
import com.api.springcore.entity.Role;
import com.api.springcore.entity.User;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.helper.RoleResolver;
import com.api.springcore.mapper.UserMapper;
import com.api.springcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleResolver roleResolver;

    @Transactional(readOnly = true)
    public Page<DomainResponse.UserSummaryDto> getUsers(String searchTerm, Pageable pageable) {

        Page<Long> idPage = userRepository.findIdsBySearch(searchTerm, pageable);

        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<User> users = userRepository.findAllByIds(idPage.getContent());

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> orderedUsers = idPage.getContent().stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<DomainResponse.UserSummaryDto> userDtoList = orderedUsers.stream()
                .map(userMapper::toSummaryDto)
                .toList();

        // wrap with correct pagination metadata
        return new PageImpl<>(userDtoList, pageable, idPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DomainResponse.UserDto getUser(Long id) {
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toDto(user);
    }

    @Transactional
    public DomainResponse.UserDto createUser(UserRequest.Create request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Set<Role> roles = roleResolver.resolve(request.getRoleIds());

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
        return userMapper.toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto updateUser(Long id, UserRequest.Update request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (StringUtils.hasText(request.getLastName())) user.setLastName(request.getLastName());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());

        user = userRepository.save(user);
        return userMapper.toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
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

        Set<Role> roles = roleResolver.resolve(request.getRoleIds());
        user.setRoles(roles);
        user = userRepository.save(user);
        return userMapper.toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto addRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> newRoles = roleResolver.resolve(request.getRoleIds());
        newRoles.forEach(user::addRole);
        user = userRepository.save(user);
        return userMapper.toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.UserDto removeRoles(Long userId, UserRequest.AssignRoles request) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Set<Role> rolesToRemove = roleResolver.resolve(request.getRoleIds());
        rolesToRemove.forEach(user::removeRole);
        user = userRepository.save(user);
        return userMapper.toDto(userRepository.findByIdWithRolesAndPermissions(user.getId()).orElseThrow());
    }
}