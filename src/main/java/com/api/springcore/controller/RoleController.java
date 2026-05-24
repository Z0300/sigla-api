package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.RoleRequest;
import com.api.springcore.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management endpoints")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('roles:read')")
    @Operation(summary = "List all roles with their permissions")
    public ResponseEntity<ApiResponse.Success<List<DomainResponse.RoleDto>>> listRoles(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<DomainResponse.RoleDto> page = roleService.getAllRoles(searchTerm, pageable);

        return ResponseEntity.ok(ApiResponse.Success.<List<DomainResponse.RoleDto>>builder()
                .data(page.getContent())
                .meta(ApiResponse.Meta.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('roles:read')")
    @Operation(summary = "Get a role by ID")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.RoleDto>builder()
                .data(roleService.getRole(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles:create')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> createRole(
            @Valid @RequestBody RoleRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<DomainResponse.RoleDto>builder()
                        .message("Role created")
                        .data(roleService.createRole(request))
                        .build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('roles:update')")
    @Operation(summary = "Update a role")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.RoleDto>builder()
                .message("Role updated")
                .data(roleService.updateRole(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles:delete')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<ApiResponse.Success<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.Success.<Void>builder()
                .message("Role deleted")
                .build());
    }

    // ── Permission assignment ─────────────────────────────────────────────────

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('roles:update')")
    @Operation(summary = "Replace all permissions for a role")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest.AssignPermissions request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.RoleDto>builder()
                .message("Permissions assigned")
                .data(roleService.assignPermissions(id, request))
                .build());
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('roles:update')")
    @Operation(summary = "Add permissions to a role (non-destructive)")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> addPermissions(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest.AssignPermissions request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.RoleDto>builder()
                .message("Permissions added")
                .data(roleService.addPermissions(id, request))
                .build());
    }

    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('roles:update')")
    @Operation(summary = "Remove permissions from a role")
    public ResponseEntity<ApiResponse.Success<DomainResponse.RoleDto>> removePermissions(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest.AssignPermissions request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.RoleDto>builder()
                .message("Permissions removed")
                .data(roleService.removePermissions(id, request))
                .build());
    }
}