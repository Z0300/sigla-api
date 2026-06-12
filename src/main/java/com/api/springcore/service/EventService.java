package com.api.springcore.service;


import com.api.springcore.dto.EventRequest;
import com.api.springcore.dto.EventResponse;
import com.api.springcore.entity.Event;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.mapper.EventMapper;
import com.api.springcore.repository.AttendeeRepository;
import com.api.springcore.repository.EventRepository;
import com.api.springcore.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

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

    @Transactional(readOnly = true)
    public Page<EventResponse.toPublicDto> getPublicEvents(String searchTerm, String status, Pageable pageable) {
        Page<Long> ids = eventRepository.findPublicEventIds(searchTerm, status, pageable);

        if (ids.isEmpty()) return Page.empty(pageable);

        List<Event> events = eventRepository.findAllByIds(ids.getContent());

        Map<Long, Long> countMap = attendeeRepository.countGroupedByEventIds(ids.getContent())
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<EventResponse.toPublicDto> eventSummary = ids.getContent().stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .map(event -> eventMapper.toPublicDto(event, countMap.getOrDefault(event.getId(), 0L)))
                .toList();

        log.info("Returning {} public event's DTO(s)", eventSummary.size());

        return new PageImpl<>(eventSummary, pageable, ids.getTotalElements());
    }

    @Transactional(readOnly = true)
    public EventResponse.toDto getEvent(Long id) {
        return eventMapper.toDto(eventRepository.findByIdWithSessions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id)));
    }


    @Transactional(readOnly = true)
    public EventResponse.toPublicSessionDto getEventWithSession(Long id) {
        Event event = eventRepository.findByIdWithSessions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));

        long registeredCount = attendeeRepository.countByEventIdAndStatusNot(id, "cancelled");

        return eventMapper.toPublicWithSessionDto(event, registeredCount);
    }

    @Transactional
    public EventResponse.toSimpleDto create(EventRequest.Create request) {
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
        return eventMapper.toSimpleDto(event);
    }


    public EventResponse.toSimpleDto updateEvent(Long eventId, EventRequest.Update request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Only draft and published can be updated
        if (!Set.of("draft", "published").contains(event.getStatus())) {
            throw new IllegalStateException(
                    "Event cannot be updated in status: " + event.getStatus()
            );
        }

        // capacity cannot be reduced below current registrations
        if (request.getCapacity() != null) {
            long registered = attendeeRepository.countByEventId(eventId);
            if (request.getCapacity() < registered) {
                throw new IllegalArgumentException(
                        "Capacity cannot be less than current registrations (" + registered + ")"
                );
            }
            event.setCapacity(request.getCapacity());
        }

        // Only apply non-null fields (partial update)
        if (request.getTitle()       != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getVenue()       != null) event.setVenue(request.getVenue());
        if (request.getStartDate()   != null) event.setStartDate(request.getStartDate());
        if (request.getEndDate()     != null) event.setEndDate(request.getEndDate());

        Event saved = eventRepository.save(event);

        // Notify attendees if sensitive fields changed on a published event
        if ("published".equals(event.getStatus())) {
            boolean sensitiveChange = request.getStartDate() != null
                    || request.getEndDate()   != null
                    || request.getVenue()     != null
                    || request.getTitle()     != null;

            if (sensitiveChange) {
                notificationService.notifyEventUpdated(saved);
            }
        }

        return eventMapper.toSimpleDto(saved);
    }

    // Status transition — separate method
    public EventResponse.toSimpleDto transitionStatus(Long eventId, String newStatus) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        validateTransition(event.getStatus(), newStatus);

        event.setStatus(newStatus);
        log.info("Event status transitioned: {} -> {}", event.getStatus(), newStatus);

        event = eventRepository.save(event);

        return eventMapper.toSimpleDto(event);
    }

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "draft",     Set.of("published", "cancelled"),
            "published", Set.of("ongoing",   "cancelled"),
            "ongoing",   Set.of("completed", "cancelled"),
            "completed", Set.of(),
            "cancelled", Set.of()
    );

    private void validateTransition(String current, String next) {
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new IllegalStateException(
                    "Cannot transition from '" + current + "' to '" + next + "'"
            );
        }
    }
}
