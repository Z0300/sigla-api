package com.api.springcore.mapper;

import com.api.springcore.dto.EventResponse;
import com.api.springcore.entity.Event;
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
}
