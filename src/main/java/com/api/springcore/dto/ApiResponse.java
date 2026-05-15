package com.api.springcore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    @Data
    @Builder
    public static class Success<T> {
        private boolean success = true;
        private String message;
        private T data;
        private Meta meta;
    }

    @Data @Builder
    public static class Error {
        private boolean success = false;
        private String message;
        private String errorCode;
        private Object details;
        private LocalDateTime timestamp;
    }

    @Data @Builder
    public static class Meta {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
