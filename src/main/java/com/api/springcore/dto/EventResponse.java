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
}
