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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Page<EventResponse.toDto> getEvents(String searchTerm, String status, Pageable pageable) {
        Page<Long> idPage = eventRepository.findIdsBySearch(searchTerm, status, pageable);

        if (idPage.isEmpty()) return Page.empty(pageable);

        List<Event> users = eventRepository.findAllByIds(idPage.getContent());

        Map<Long, Event> eventMap = users.stream()
                .collect(Collectors.toMap(Event::getId, u -> u));

        List<EventResponse.toDto> summaryDtoList = idPage.getContent().stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .map(eventMapper::toDto)
                .toList();

        log.info("Returning {} event DTO(s)", summaryDtoList.size());

        return new PageImpl<>(summaryDtoList, pageable, idPage.getTotalElements());
    }


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
