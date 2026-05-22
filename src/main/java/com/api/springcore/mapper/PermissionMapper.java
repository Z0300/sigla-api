package com.api.springcore.mapper;

import com.api.springcore.dto.DomainResponse;
import com.api.springcore.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {
    public DomainResponse.PermissionDto toDto(Permission permission) {
        return DomainResponse.PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
