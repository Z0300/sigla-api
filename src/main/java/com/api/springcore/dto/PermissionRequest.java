package com.api.springcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class PermissionRequest {
    @Data
    public static class Create {
        @NotBlank(message = "Permission name is required")
        @Size(max = 150)
        @Pattern(regexp = "^[a-z][a-z0-9_]*:[a-z][a-z0-9_]*$",
                message = "Permission name must follow pattern: resource:action (e.g. users:read)")
        private String name;

        @Size(max = 500)
        private String description;
    }

    @Data
    public static class Update {
        @Size(max = 150)
        @Pattern(regexp = "^[a-z][a-z0-9_]*:[a-z][a-z0-9_]*$",
                message = "Permission name must follow pattern: resource:action (e.g. users:read)")
        private String name;

        @Size(max = 500)
        private String description;
    }
}
