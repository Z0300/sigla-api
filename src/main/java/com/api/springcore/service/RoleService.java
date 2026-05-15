package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.RoleRequest;
import com.api.springcore.entity.Permission;
import com.api.springcore.entity.Role;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.repository.PermissionRepository;
import com.api.springcore.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<DomainResponse.RoleDto> getAllRoles() {
        return roleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DomainResponse.RoleDto getRole(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        return toDto(role);
    }

    @Transactional
    public DomainResponse.RoleDto createRole(RoleRequest.Create request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role already exists: " + request.getName());
        }

        Set<Permission> permissions = resolvePermissions(request.getPermissionIds());
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();

        role = roleRepository.save(role);
        log.info("Role created: {}", role.getName());
        return toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
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
        return toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
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

        Set<Permission> permissions = resolvePermissions(request.getPermissionIds());
        role.setPermissions(permissions);
        roleRepository.save(role);
        return toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.RoleDto addPermissions(Long roleId, RoleRequest.AssignPermissions request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        resolvePermissions(request.getPermissionIds()).forEach(role::addPermission);
        roleRepository.save(role);
        return toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    @Transactional
    public DomainResponse.RoleDto removePermissions(Long roleId, RoleRequest.AssignPermissions request) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        resolvePermissions(request.getPermissionIds()).forEach(role::removePermission);
        roleRepository.save(role);
        return toDto(roleRepository.findByIdWithPermissions(role.getId()).orElseThrow());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Set<Permission> resolvePermissions(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Permission> found = permissionRepository.findAllByIdIn(ids);
        if (found.size() != ids.size()) {
            throw new BadRequestException("One or more permission IDs are invalid");
        }
        return new HashSet<>(found);
    }

    public DomainResponse.RoleDto toDto(Role role) {
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