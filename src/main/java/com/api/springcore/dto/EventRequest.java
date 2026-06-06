package com.api.springcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class EventRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        @NotBlank(message = "Title is required")
        @Size(max = 255)
        String title;

        @NotBlank(message = "Venue is required")
        @Size(max = 255)
        String venue;

        @NotNull(message = "Start date is required")
        LocalDateTime startDate;

        @NotNull(message = "End date is required")
        LocalDateTime endDate;

        @Builder.Default
        Integer capacity = 0;

        String description;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        @NotBlank(message = "Title is required")
        @Size(max = 255)
        String title;

        @NotBlank(message = "Venue is required")
        @Size(max = 255)
        String venue;

        @NotNull(message = "Start date is required")
        LocalDateTime startDate;

        @NotNull(message = "End date is required")
        LocalDateTime endDate;

        @Builder.Default
        Integer capacity = 0;

        String description;
    }
}
