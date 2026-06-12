package com.api.springcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponse {

    @Data
    @Builder
    public static class toSimpleDto {
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

    @Data
    @Builder
    public static class toPublicDto {
        private Long id;
        private String title;
        private String description;
        private String venue;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer capacity;
        private String status;
        private long registeredCount;
    }

    @Data
    @Builder
    public static class toPublicSessionDto {
        private Long id;
        private String title;
        private String description;
        private String venue;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer capacity;
        private String status;
        private long registeredCount;
        private List<SessionResponse.Summary> sessions;
    }
}
