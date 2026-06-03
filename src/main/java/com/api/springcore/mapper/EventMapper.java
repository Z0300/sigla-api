package com.api.springcore.mapper;

import com.api.springcore.dto.EventResponse;
import com.api.springcore.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponse.CreateDto newEventDto(Event event) {
        return EventResponse.CreateDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
