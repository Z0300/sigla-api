package com.api.springcore.helper;

import com.api.springcore.entity.Permission;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionResolver {

    private final PermissionRepository permissionRepository;

    public Set<Permission> resolve(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        List<Permission> found = permissionRepository.findAllByIdIn(ids);
        if (found.size() != ids.size()) {
            throw new BadRequestException("One or more permission IDs are invalid");
        }
        return new HashSet<>(found);
    }
}