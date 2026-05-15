package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.PermissionRequest;
import com.api.springcore.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management endpoints")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('permissions:read')")
    @Operation(summary = "List all permissions")
    public ResponseEntity<ApiResponse.Success<List<DomainResponse.PermissionDto>>> listPermissions() {
        return ResponseEntity.ok(ApiResponse.Success.<List<DomainResponse.PermissionDto>>builder()
                .data(permissionService.getAllPermissions())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:read')")
    @Operation(summary = "Get a permission by ID")
    public ResponseEntity<ApiResponse.Success<DomainResponse.PermissionDto>> getPermission(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.PermissionDto>builder()
                .data(permissionService.getPermission(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permissions:create')")
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse.Success<DomainResponse.PermissionDto>> createPermission(
            @Valid @RequestBody PermissionRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<DomainResponse.PermissionDto>builder()
                        .message("Permission created")
                        .data(permissionService.createPermission(request))
                        .build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:update')")
    @Operation(summary = "Update a permission")
    public ResponseEntity<ApiResponse.Success<DomainResponse.PermissionDto>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.PermissionDto>builder()
                .message("Permission updated")
                .data(permissionService.updatePermission(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:delete')")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<ApiResponse.Success<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.Success.<Void>builder()
                .message("Permission deleted")
                .build());
    }
}