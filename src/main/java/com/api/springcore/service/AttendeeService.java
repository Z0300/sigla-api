package com.api.springcore.service;

import com.api.springcore.dto.AttendeeResponse;
import com.api.springcore.entity.Attendee;
import com.api.springcore.entity.Event;
import com.api.springcore.entity.QrToken;
import com.api.springcore.entity.User;
import com.api.springcore.exception.BadRequestException;
import com.api.springcore.exception.DuplicateResourceException;
import com.api.springcore.exception.ForbiddenException;
import com.api.springcore.exception.ResourceNotFoundException;
import com.api.springcore.mapper.AttendeeMapper;
import com.api.springcore.repository.AttendeeRepository;
import com.api.springcore.repository.EventRepository;
import com.api.springcore.repository.QrTokenRepository;
import com.api.springcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendeeService {
    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final QrTokenRepository qrTokenRepository;
    private final AttendeeMapper attendeeMapper;
    private final NotificationService notificationService;

    // Events must be in one of these to allow registration
    private static final Set<String> REGISTERABLE_STATUSES = Set.of("published", "ongoing");


    @Transactional
    public AttendeeResponse.Simple register(Long eventId, Long currentUserId) {
        Event event = findEventOrThrow(eventId);
        User user  = findUserOrThrow(currentUserId);

        validateRegisterable(event);
        validateNotAlreadyRegistered(currentUserId, eventId);
        validateCapacity(event);

        Attendee attendee = saveAttendee(user, event);
        QrToken  qrToken  = generateQrToken(attendee, event);

        notificationService.notifyRegistrationConfirmed(attendee, event);

        log.info("Attendee registered: userId={} eventId={}", currentUserId, eventId);
        return attendeeMapper.toSimpleDto(attendee, qrToken);
    }


    @Transactional
    public AttendeeResponse.Simple registerUser(Long eventId, Long targetUserId, Long currentUserId) {
        Event event      = findEventOrThrow(eventId);
        validateOrganizerOrAdmin(event, currentUserId);

        User targetUser = findUserOrThrow(targetUserId);

        validateRegisterable(event);
        validateNotAlreadyRegistered(targetUserId, eventId);
        validateCapacity(event);

        Attendee attendee = saveAttendee(targetUser, event);
        QrToken  qrToken  = generateQrToken(attendee, event);

        notificationService.notifyRegistrationConfirmed(attendee, event);

        log.info("Attendee registered by organizer: userId={} eventId={} by={}",
                targetUserId, eventId, currentUserId);
        return attendeeMapper.toSimpleDto(attendee, qrToken);
    }


    @Transactional
    public AttendeeResponse.Simple cancelRegistration(Long eventId, Long attendeeId, Long currentUserId) {
        Attendee attendee = attendeeRepository.findByUserIdAndEventId(attendeeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        Event event = attendee.getEvent();

        // Only the attendee themselves or organizer/admin can cancel
        boolean isSelf      = attendee.getUser().getId().equals(currentUserId);
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUserId);
        if (!isSelf && !isOrganizer) {
            throw new ForbiddenException("You cannot cancel this registration");
        }

        if ("cancelled".equals(attendee.getStatus())) {
            throw new BadRequestException("Registration is already cancelled");
        }

        if ("no_show".equals(attendee.getStatus())) {
            throw new BadRequestException("Cannot cancel a no-show registration");
        }

        // Invalidate their QR token
        qrTokenRepository.findByAttendeeIdAndUsedFalse(attendeeId)
                .ifPresent(qr -> {
                    qr.setUsed(true);
                    qrTokenRepository.save(qr);
                });

        attendee.setStatus("cancelled");
        attendee = attendeeRepository.save(attendee);

        log.info("Registration cancelled: attendeeId={} by={}", attendeeId, currentUserId);
        return attendeeMapper.toSimpleDto(attendee, null);
    }


    @Transactional(readOnly = true)
    public Page<AttendeeResponse.Summary> getAttendees(Long eventId, String status,
                                                       Long currentUserId, Pageable pageable) {
        Event event = findEventOrThrow(eventId);
        validateOrganizerOrAdmin(event, currentUserId);

        return attendeeRepository
                .findByEventIdAndStatus(eventId, status, pageable)
                .map(attendeeMapper::toSummaryDto);
    }


    @Transactional(readOnly = true)
    public AttendeeResponse.Simple getMyRegistration(Long eventId, Long currentUserId) {
        Attendee attendee = attendeeRepository.findByUserIdAndEventId(currentUserId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not registered for this event"));

        QrToken qrToken = qrTokenRepository
                .findByAttendeeIdAndUsedFalse(attendee.getId())
                .orElse(null);

        return attendeeMapper.toSimpleDto(attendee, qrToken);
    }


    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateRegisterable(Event event) {
        if (!REGISTERABLE_STATUSES.contains(event.getStatus())) {
            throw new BadRequestException(
                    "Registration is not open for this event (status: " + event.getStatus() + ")"
            );
        }
    }

    private void validateNotAlreadyRegistered(Long userId, Long eventId) {
        if (attendeeRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new DuplicateResourceException("User is already registered for this event");
        }
    }

    private void validateCapacity(Event event) {
        if (event.getCapacity() != null) {
            long registered = attendeeRepository.countByEventId(event.getId());
            if (registered >= event.getCapacity()) {
                throw new BadRequestException("Event is at full capacity");
            }
        }
    }

    private void validateOrganizerOrAdmin(Event event, Long currentUserId) {
        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the event organizer can perform this action");
        }
    }

    private Attendee saveAttendee(User user, Event event) {
        Attendee attendee = Attendee.builder()
                .user(user)
                .event(event)
                .status("registered")
                .build();
        return attendeeRepository.save(attendee);
    }

    private QrToken generateQrToken(Attendee attendee, Event event) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        QrToken qrToken = QrToken.builder()
                .attendee(attendee)
                .token(token)
                .type("normal")
                .used(false)
                .expiresAt(event.getEndDate()) // valid until event ends
                .build();

        return qrTokenRepository.save(qrToken);
    }
}
