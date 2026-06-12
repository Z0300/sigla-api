package com.api.springcore.mapper;

import com.api.springcore.dto.EventResponse;
import com.api.springcore.dto.SessionResponse;
import com.api.springcore.entity.Event;
import com.api.springcore.entity.Session;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponse.toSimpleDto toSimpleDto(Event event) {
        return EventResponse.toSimpleDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public EventResponse.toDto toDto(Event event) {
        return EventResponse.toDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .build();
    }

    public EventResponse.toPublicDto toPublicDto(Event event, long registeredCount) {
        return EventResponse.toPublicDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .registeredCount(registeredCount)
                .build();
    }

    public EventResponse.toPublicSessionDto toPublicWithSessionDto(Event event, long registeredCount) {
        return EventResponse.toPublicSessionDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .venue(event.getVenue())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .registeredCount(registeredCount)
                .sessions(
                        event.getSessions().stream()
                                .map(this::toSessionDto)
                                .toList()
                )
                .build();
    }

    private SessionResponse.Summary toSessionDto(Session session) {
        return new SessionResponse.Summary(
                session.getId(),
                session.getTitle(),
                session.getRoom(),
                session.getStartTime(),
                session.getEndTime(),
                session.getCapacity()
        );
    }

}
