package com.api.springcore.service;

import com.api.springcore.entity.Attendee;
import com.api.springcore.entity.Event;
import com.api.springcore.repository.AttendeeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final AttendeeRepository attendeeRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");

    // -------------------------------------------------------------------------
    // Called by EventService.updateEvent() when sensitive fields change
    // -------------------------------------------------------------------------

    @Async
    public void notifyEventUpdated(Event event) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping notifyEventUpdated for event {}", event.getId());
            return;
        }
        List<Attendee> attendees = attendeeRepository
                .findAllByEventIdAndStatus(event.getId(), "registered");

        if (attendees.isEmpty()) return;

        for (Attendee attendee : attendees) {
            try {
                sendEventUpdatedEmail(attendee, event);
            } catch (Exception e) {
                // Log and continue — don't fail the whole batch for one bad email
                log.error("Failed to send update notification to attendee {}: {}",
                        attendee.getId(), e.getMessage());
            }
        }

        log.info("Sent event-updated notification to {} attendees for event {}",
                attendees.size(), event.getId());
    }

    // -------------------------------------------------------------------------
    // Called by EventService.transitionStatus() when event is cancelled
    // -------------------------------------------------------------------------

    @Async
    public void notifyEventCancelled(Event event) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping notifyEventCancelled for event {}", event.getId());
            return;
        }
        List<Attendee> attendees = attendeeRepository
                .findAllByEventIdAndStatus(event.getId(), "registered");

        if (attendees.isEmpty()) return;

        for (Attendee attendee : attendees) {
            try {
                sendEventCancelledEmail(attendee, event);
            } catch (Exception e) {
                log.error("Failed to send cancellation notification to attendee {}: {}",
                        attendee.getId(), e.getMessage());
            }
        }

        log.info("Sent event-cancelled notification to {} attendees for event {}",
                attendees.size(), event.getId());
    }

    // -------------------------------------------------------------------------
    // Called by registration flow after successful attendee registration
    // -------------------------------------------------------------------------

    @Async
    public void notifyRegistrationConfirmed(Attendee attendee, Event event) {
        try {
            sendRegistrationConfirmedEmail(attendee, event);
        } catch (Exception e) {
            log.error("Failed to send registration confirmation to attendee {}: {}",
                    attendee.getId(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private email builders
    // -------------------------------------------------------------------------

    private void sendEventUpdatedEmail(Attendee attendee, Event event)
            throws MessagingException {
        String email = attendee.getUser().getEmail();
        String name = attendee.getUser().getFirstName();

        String subject = "Update: " + event.getTitle();
        String body = """
                <p>Hi %s,</p>
                <p>The event <strong>%s</strong> you registered for has been updated.</p>
                <table>
                  <tr><td><strong>Venue</strong></td><td>%s</td></tr>
                  <tr><td><strong>Start</strong></td><td>%s</td></tr>
                  <tr><td><strong>End</strong></td><td>%s</td></tr>
                </table>
                <p>Please review the changes and update your plans accordingly.</p>
                """.formatted(
                name,
                event.getTitle(),
                event.getVenue(),
                event.getStartDate().format(DATE_FORMAT),
                event.getEndDate().format(DATE_FORMAT)
        );

        sendHtmlEmail(email, subject, body);
    }

    private void sendEventCancelledEmail(Attendee attendee, Event event)
            throws MessagingException {
        String email = attendee.getUser().getEmail();
        String name = attendee.getUser().getFirstName();

        String subject = "Cancelled: " + event.getTitle();
        String body = """
                <p>Hi %s,</p>
                <p>We're sorry to inform you that <strong>%s</strong> has been cancelled.</p>
                <p>If you have any questions, please contact the organizer.</p>
                """.formatted(name, event.getTitle());

        sendHtmlEmail(email, subject, body);
    }

    private void sendRegistrationConfirmedEmail(Attendee attendee, Event event)
            throws MessagingException {
        String email = attendee.getUser().getEmail();
        String name = attendee.getUser().getFirstName();

        String subject = "You're registered: " + event.getTitle();
        String body = """
                <p>Hi %s,</p>
                <p>You're registered for <strong>%s</strong>.</p>
                <table>
                  <tr><td><strong>Venue</strong></td><td>%s</td></tr>
                  <tr><td><strong>Start</strong></td><td>%s</td></tr>
                  <tr><td><strong>End</strong></td><td>%s</td></tr>
                </table>
                <p>Your QR ticket is attached. Present it at the entrance to check in.</p>
                """.formatted(
                name,
                event.getTitle(),
                event.getVenue(),
                event.getStartDate().format(DATE_FORMAT),
                event.getEndDate().format(DATE_FORMAT)
        );

        sendHtmlEmail(email, subject, body);
    }

    // -------------------------------------------------------------------------
    // Core mail sender
    // -------------------------------------------------------------------------

    private void sendHtmlEmail(String to, String subject, String htmlBody)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = isHtml
        // helper.setFrom("no-reply@yourapp.com"); // set in application.properties

        mailSender.send(message);
    }
}