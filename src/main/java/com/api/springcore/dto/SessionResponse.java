package com.api.springcore.dto;

import java.time.LocalDateTime;

public class SessionResponse {
    public record Simple(
            Long id,
            Long eventId,
            String eventTitle,
            String title,
            String room,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer capacity,
            long checkInCount,
            LocalDateTime createdAt
    ) {}

    public record Detail(
            Long id,
            Long eventId,
            String eventTitle,
            String title,
            String room,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer capacity,
            long checkInCount,
            String sessionQrToken,
            LocalDateTime qrExpiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
