package com.api.springcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponse {

    @Data
    @Builder
    public static class CreateDto {
        private Long id;
        private String title;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class toDto {
        private Long id;
        private String title;
        private String description;
        private String venue;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer capacity;
        private String status;
    }
}
