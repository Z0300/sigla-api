package com.api.springcore.mapper;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public DomainResponse.RoleDto toDto(Role role) {
        return DomainResponse.RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(permissionMapper::toDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public DomainResponse.RoleSummaryDto toSummaryDto(Role role) {
        return DomainResponse.RoleSummaryDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}