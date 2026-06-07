package com.api.springcore.service;

import com.api.springcore.dto.SessionRequest;
import com.api.springcore.dto.SessionResponse;
import com.api.springcore.entity.Event;
import com.api.springcore.entity.Session;
import com.api.springcore.entity.SessionQr;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.ForbiddenException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.mapper.SessionMapper;
import com.api.springcore.repository.EventRepository;
import com.api.springcore.repository.SessionQrRepository;
import com.api.springcore.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository     sessionRepository;
    private final SessionQrRepository   sessionQrRepository;
    private final EventRepository       eventRepository;
    private final SessionMapper         sessionMapper;

    private static final Set<String> EDITABLE_EVENT_STATUSES = Set.of("draft", "published");


    @Transactional
    public SessionResponse.Simple createSession(Long eventId, SessionRequest.Create request, Long currentUserId) {
        Event event = findEventOrThrow(eventId);

        validateEventOwnership(event, currentUserId);
        validateEventEditable(event);
        validateSessionTimes(request.startTime(), request.endTime(), event);
        validateNoTimeConflict(eventId, request.room(), request.startTime(), request.endTime(), 0L);

        Session session = Session.builder()
                .event(event)
                .title(request.title())
                .room(request.room())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .capacity(request.capacity())
                .build();

        session = sessionRepository.save(session);
        log.info("Session created: id={} event={}", session.getId(), eventId);

        return sessionMapper.toSimpleDto(session, 0L);
    }


    @Transactional(readOnly = true)
    public List<SessionResponse.Simple> getSessionsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found");
        }

        return sessionRepository.findAllByEventIdOrderByStartTimeAsc(eventId)
                .stream()
                .map(s -> sessionMapper.toSimpleDto(s,
                        sessionRepository.countCheckInsBySessionId(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse.Detail getSession(Long eventId, Long sessionId, Long currentUserId) {
        Session session = findSessionOrThrow(sessionId);
        verifySessionBelongsToEvent(session, eventId);

        long checkInCount = sessionRepository.countCheckInsBySessionId(sessionId);

        // Only expose QR token to the event organizer or admin
        SessionQr sessionQr = null;
        if (isOrganizerOrAdmin(session.getEvent(), currentUserId)) {
            sessionQr = sessionQrRepository
                    .findBySessionIdAndActiveTrue(sessionId)
                    .filter(SessionQr::isUsable)
                    .orElse(null);
        }

        return sessionMapper.toDetailDto(session, checkInCount, sessionQr);
    }


    @Transactional
    public SessionResponse.Simple updateSession(Long eventId, Long sessionId,
                                           SessionRequest.Update request, Long currentUserId) {
        Session session = findSessionOrThrow(sessionId);
        verifySessionBelongsToEvent(session, eventId);

        Event event = session.getEvent();
        validateEventOwnership(event, currentUserId);
        validateEventEditable(event);

        LocalDateTime effectiveStart = request.startTime() != null ? request.startTime() : session.getStartTime();
        LocalDateTime effectiveEnd   = request.endTime()   != null ? request.endTime()   : session.getEndTime();
        String effectiveRoom         = request.room()      != null ? request.room()       : session.getRoom();

        validateSessionTimes(effectiveStart, effectiveEnd, event);
        validateNoTimeConflict(eventId, effectiveRoom, effectiveStart, effectiveEnd, sessionId);

        if (request.title()    != null) session.setTitle(request.title());
        if (request.room()     != null) session.setRoom(request.room());
        if (request.startTime() != null) session.setStartTime(request.startTime());
        if (request.endTime()  != null) session.setEndTime(request.endTime());
        if (request.capacity() != null) {
            validateCapacityReduction(sessionId, request.capacity());
            session.setCapacity(request.capacity());
        }

        session = sessionRepository.save(session);
        log.info("Session updated: id={}", sessionId);

        long checkInCount = sessionRepository.countCheckInsBySessionId(sessionId);
        return sessionMapper.toSimpleDto(session, checkInCount);
    }


    @Transactional
    public void deleteSession(Long eventId, Long sessionId, Long currentUserId) {
        Session session = findSessionOrThrow(sessionId);
        verifySessionBelongsToEvent(session, eventId);

        validateEventOwnership(session.getEvent(), currentUserId);
        validateEventEditable(session.getEvent());

        long checkIns = sessionRepository.countCheckInsBySessionId(sessionId);
        if (checkIns > 0) {
            throw new BadRequestException(
                    "Cannot delete session with existing check-ins (" + checkIns + " records)"
            );
        }

        sessionRepository.delete(session);
        log.info("Session deleted: id={} event={}", sessionId, eventId);
    }


    @Transactional
    public SessionResponse.Detail generateSessionQr(Long eventId, Long sessionId, Long currentUserId) {
        Session session = findSessionOrThrow(sessionId);
        verifySessionBelongsToEvent(session, eventId);
        validateEventOwnership(session.getEvent(), currentUserId);

        if (!"ongoing".equals(session.getEvent().getStatus())) {
            throw new BadRequestException("QR can only be generated while the event is ongoing");
        }


        sessionQrRepository.deactivateAllBySessionId(sessionId);

        SessionQr qr = SessionQr.builder()
                .session(session)
                .token(generateSecureToken())
                .active(true)
                .expiresAt(session.getEndTime())
                .build();

        qr = sessionQrRepository.save(qr);
        log.info("Session QR generated: sessionId={} expiresAt={}", sessionId, qr.getExpiresAt());

        long checkInCount = sessionRepository.countCheckInsBySessionId(sessionId);
        return sessionMapper.toDetailDto(session, checkInCount, qr);
    }


    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private Session findSessionOrThrow(Long sessionId) {
        return sessionRepository.findByIdWithEvent(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private void verifySessionBelongsToEvent(Session session, Long eventId) {
        if (!session.getEvent().getId().equals(eventId)) {
            throw new ResourceNotFoundException("Session not found for this event");
        }
    }

    private void validateEventOwnership(Event event, Long currentUserId) {
        boolean isAdmin = false;
        if (!event.getOrganizer().getId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException("You are not the organizer of this event");
        }
    }

    private boolean isOrganizerOrAdmin(Event event, Long currentUserId) {
        return event.getOrganizer().getId().equals(currentUserId);
        // extend with admin role check as needed
    }

    private void validateEventEditable(Event event) {
        if (!EDITABLE_EVENT_STATUSES.contains(event.getStatus())) {
            throw new BadRequestException(
                    "Sessions cannot be modified when event is '" + event.getStatus() + "'"
            );
        }
    }

    private void validateSessionTimes(LocalDateTime start, LocalDateTime end, Event event) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }
        if (start.isBefore(event.getStartDate())) {
            throw new BadRequestException("Session cannot start before the event starts");
        }
        if (end.isAfter(event.getEndDate())) {
            throw new BadRequestException("Session cannot end after the event ends");
        }
    }

    private void validateNoTimeConflict(Long eventId, String room,
                                        LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (sessionRepository.existsTimeConflict(eventId, room, start, end, excludeId)) {
            throw new BadRequestException(
                    "Room '" + room + "' is already booked during this time slot"
            );
        }
    }

    private void validateCapacityReduction(Long sessionId, int newCapacity) {
        long checkIns = sessionRepository.countCheckInsBySessionId(sessionId);
        if (newCapacity < checkIns) {
            throw new BadRequestException(
                    "Capacity cannot be less than current check-ins (" + checkIns + ")"
            );
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}