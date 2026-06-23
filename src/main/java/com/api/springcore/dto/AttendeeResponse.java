package com.api.springcore.dto;

import java.time.LocalDateTime;

public class AttendeeResponse {
    public record Simple(
            Long id,
            Long eventId,
            String eventTitle,
            Long userId,
            String userFullName,
            String userEmail,
            String status,
            String qrToken,
            LocalDateTime registeredAt,
            long registeredCount
    ) {}

    public record Ticket(
            Long attendeeId,
            Long eventId,
            String eventTitle,
            String eventVenue,
            String eventStatus,
            LocalDateTime eventStartDate,
            LocalDateTime eventEndDate,
            String attendeeStatus,
            LocalDateTime registeredAt
    ) {}

    public record Summary(
            Long id,
            Long userId,
            String userFullName,
            String userEmail,
            String status,
            LocalDateTime registeredAt
    ) {}
}
