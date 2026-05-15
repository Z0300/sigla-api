package com.api.springcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainResponse {

    @Data @Builder
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Set<RoleDto> roles;
    }

    @Data @Builder
    public static class UserSummaryDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    @Data @Builder
    public static class RoleDto {
        private Long id;
        private String name;
        private String description;
        private Set<PermissionDto> permissions;
    }

    @Data @Builder
    public static class RoleSummaryDto {
        private Long id;
        private String name;
        private String description;
    }

    @Data @Builder
    public static class PermissionDto {
        private Long id;
        private String name;
        private String description;
    }

    @Data @Builder
    public static class TokenDto {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserSummaryDto user;
    }

    @Data @Builder
    public static class SocialAccountDto {
        private Long id;
        private String provider;
        private String providerUserId;
        private LocalDateTime createdAt;
    }
}
