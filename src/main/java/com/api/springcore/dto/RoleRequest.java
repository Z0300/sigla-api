package com.api.springcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

public class RoleRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Role name is required")
        @Size(max = 100)
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$",
                message = "Role name must be uppercase letters, digits, or underscores")
        private String name;

        @Size(max = 500)
        private String description;

        private Set<Long> permissionIds;
    }

    @Data
    public static class Update {
        @Size(max = 100)
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$",
                message = "Role name must be uppercase letters, digits, or underscores")
        private String name;

        @Size(max = 500)
        private String description;
    }

    @Data
    public static class AssignPermissions {
        @NotEmpty(message = "At least one permission ID is required")
        private Set<Long> permissionIds;
    }
}
