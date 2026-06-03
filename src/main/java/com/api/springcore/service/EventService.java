package com.api.springcore.service;

import com.api.springcore.dto.EventRequest;
import com.api.springcore.dto.EventResponse;
import com.api.springcore.entity.Event;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.mapper.EventMapper;
import com.api.springcore.repository.EventRepository;
import com.api.springcore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    public EventResponse.CreateDto create(EventRequest.Create request) {
        if (eventRepository.existsByTitle(request.getTitle())) {
            throw new DuplicateResourceException("Title already exists: " + request.getTitle());
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .venue(request.getVenue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .organizer(securityUtils.getCurrentUser())
                .build();
        event = eventRepository.save(event);
        log.info("Event created: {}", event.getTitle());
        return eventMapper.newEventDto(event);
    }
}
