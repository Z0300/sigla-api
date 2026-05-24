package com.api.springcore.service;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.PermissionRequest;
import com.api.springcore.entity.Permission;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.mapper.PermissionMapper;
import com.api.springcore.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;



@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    public Page<DomainResponse.PermissionDto> getAllPermissions(String searchTerm, Pageable pageable) {
        return permissionRepository.findAllWithSearch(searchTerm, pageable)
                .map(permissionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public DomainResponse.PermissionDto getPermission(Long id) {
        return permissionMapper.toDto(permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id)));
    }

    @Transactional
    public DomainResponse.PermissionDto createPermission(PermissionRequest.Create request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Permission already exists: " + request.getName());
        }
        Permission perm = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        perm = permissionRepository.save(perm);
        log.info("Permission created: {}", perm.getName());
        return permissionMapper.toDto(perm);
    }

    @Transactional
    public DomainResponse.PermissionDto updatePermission(Long id, PermissionRequest.Update request) {
        Permission perm = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(perm.getName())) {
            if (permissionRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Permission already exists: " + request.getName());
            }
            perm.setName(request.getName());
        }
        if (request.getDescription() != null) perm.setDescription(request.getDescription());

        return permissionMapper.toDto(permissionRepository.save(perm));
    }

    @Transactional
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission", id);
        }
        permissionRepository.deleteById(id);
        log.info("Permission deleted: {}", id);
    }
}