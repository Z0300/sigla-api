package com.api.springcore.dto;

import jakarta.validation.constraints.NotNull;

public class AttendeeRequest {
    public record Register() {}

    public record RegisterUser(
            @NotNull(message = "User ID is required")
            Long userId
    ) {}

    public record Cancel() {}
}
