package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.RoleRequest;
import com.api.springcore.entity.Permission;
import com.api.springcore.entity.Role;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.helper.PermissionResolver;

import com.api.springcore.mapper.RoleMapper;
import com.api.springcore.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionResolver permissionResolver;

    @Transactional(readOnly = true)
    public Page<DomainResponse.RoleDto> getAllRoles(String searchTerm, Pageable pageable) {
        Page<Long> idPage = roleRepository.findIdsBySearch(searchTerm, pageable);

        if (idPage.isEmpty()) return Page.empty(pageable);

        List<Role> roles = roleRepository.findAllByIds(idPage.getContent());

        Map<Long, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        List<Role> ordered = idPage.getContent().stream()
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<DomainResponse.RoleDto> roleDtoList = ordered.stream()
                .map(roleMapper::toDto)
                .toList();

        return new PageImpl<>(roleDtoList, pageable, idPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DomainResponse.RoleDto getRole(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        return roleMapper.toDto(role);
    }

    @Transactional
    public DomainResponse.RoleDto createRole(RoleRequest.Create request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role already exists: " + request.getName());
        }

        Set<Permission> permissions = permissionResolver.resolve(request.getPermissionIds());
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();

        role = roleRepository.save(role);
        log.info("Role created: {}", role.getName());
        return roleMapper.toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.RoleDto updateRole(Long id, RoleRequest.Update request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Role already exists: " + request.getName());
            }
            role.setName(request.getName());
        }
        if (request.getDescription() != null) role.setDescription(request.getDescription());

        role = roleRepository.save(role);
        return roleMapper.toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if (List.of("SUPER_ADMIN", "USER").contains(role.getName())) {
            throw new BadRequestException("Cannot delete system role: " + role.getName());
        }
        roleRepository.deleteById(id);
        log.info("Role deleted: {} ({})", role.getName(), id);
    }

    @Transactional
    public DomainResponse.RoleDto assignPermissions(Long roleId, RoleRequest.AssignPermissions request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        Set<Permission> permissions = permissionResolver.resolve(request.getPermissionIds());
        role.setPermissions(permissions);
        roleRepository.save(role);
        return roleMapper.toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.RoleDto addPermissions(Long roleId, RoleRequest.AssignPermissions request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        permissionResolver.resolve(request.getPermissionIds()).forEach(role::addPermission);
        roleRepository.save(role);
        return roleMapper.toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.RoleDto removePermissions(Long roleId, RoleRequest.AssignPermissions request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        permissionResolver.resolve(request.getPermissionIds()).forEach(role::removePermission);
        roleRepository.save(role);
        return roleMapper.toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

}