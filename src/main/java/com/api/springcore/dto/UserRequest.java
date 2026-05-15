package com.api.springcore.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

public class UserRequest {
    @Data
    public static class Create {
        @Email
        @NotBlank
        @Size(max = 255)
        private String email;

        @NotBlank @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
                message = "Password must contain uppercase, lowercase, digit, and special character")
        private String password;

        @NotBlank @Size(max = 100)
        private String firstName;

        @NotBlank @Size(max = 100)
        private String lastName;

        private Set<Long> roleIds;
    }

    @Data
    public static class Update {
        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        private Boolean isActive;
    }

    @Data
    public static class AssignRoles {
        @NotEmpty(message = "At least one role ID is required")
        private Set<Long> roleIds;
    }
}
