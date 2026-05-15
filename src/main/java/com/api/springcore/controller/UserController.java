package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.DomainResponse;
import com.api.springcore.dto.UserRequest;
import com.api.springcore.security.CustomUserDetailsService.UserPrincipal;
import com.api.springcore.service.UserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    // ── Profile (any authenticated user) ─────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    @PreAuthorize("hasAuthority('profile:read')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .data(userService.getUser(principal.id()))
                .build());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user's profile")
    @PreAuthorize("hasAuthority('profile:update')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .message("Profile updated")
                .data(userService.updateUser(principal.id(), request))
                .build());
    }

    // ── Admin user management ─────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all users (paginated, searchable)")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ApiResponse.Success<Page<DomainResponse.UserSummaryDto>>> listUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<DomainResponse.UserSummaryDto> page = userService.getUsers(search, pageable);
        return ResponseEntity.ok(ApiResponse.Success.<Page<DomainResponse.UserSummaryDto>>builder()
                .data(page)
                .meta(ApiResponse.Meta.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .data(userService.getUser(id))
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a new user (admin)")
    @PreAuthorize("hasAuthority('users:create')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> createUser(
            @Valid @RequestBody UserRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<DomainResponse.UserDto>builder()
                        .message("User created")
                        .data(userService.createUser(request))
                        .build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a user by ID")
    @PreAuthorize("hasAuthority('users:update')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.Update request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .message("User updated")
                .data(userService.updateUser(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by ID")
    @PreAuthorize("hasAuthority('users:delete')")
    public ResponseEntity<ApiResponse.Success<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.Success.<Void>builder()
                .message("User deleted")
                .build());
    }

    // ── Role assignment ───────────────────────────────────────────────────────

    @PutMapping("/{id}/roles")
    @Operation(summary = "Replace all roles for a user")
    @PreAuthorize("hasAuthority('users:update')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.AssignRoles request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .message("Roles assigned")
                .data(userService.assignRoles(id, request))
                .build());
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Add roles to a user (non-destructive)")
    @PreAuthorize("hasAuthority('users:update')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> addRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.AssignRoles request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .message("Roles added")
                .data(userService.addRoles(id, request))
                .build());
    }

    @DeleteMapping("/{id}/roles")
    @Operation(summary = "Remove roles from a user")
    @PreAuthorize("hasAuthority('users:update')")
    public ResponseEntity<ApiResponse.Success<DomainResponse.UserDto>> removeRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.AssignRoles request) {
        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.UserDto>builder()
                .message("Roles removed")
                .data(userService.removeRoles(id, request))
                .build());
    }
}