package com.api.springcore.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class SessionRequest {
    public record Create(@NotBlank(message = "Title is required") String title,

                         @NotBlank(message = "Room is required") String room,

                         @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") LocalDateTime startTime,

                         @NotNull(message = "End time is required") @Future(message = "End time must be in the future") LocalDateTime endTime,

                         @NotNull(message = "Capacity is required") @Min(value = 1, message = "Capacity must be at least 1") Integer capacity) {
    }

    public record Update(
            String title,
            String room,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer capacity
    ) {}
}
