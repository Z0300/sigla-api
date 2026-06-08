package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.AttendeeResponse;
import com.api.springcore.security.CustomUserDetailsService;
import com.api.springcore.service.AttendeeService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events/{eventId}/attendees")
@RequiredArgsConstructor
public class AttendeeController {
    private final AttendeeService attendeeService;


    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse.Success<AttendeeResponse.Simple>> register(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        AttendeeResponse.Simple attendee = attendeeService.register(eventId, currentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<AttendeeResponse.Simple>builder()
                        .message("Successfully registered for the event")
                        .data(attendee)
                        .build()
        );
    }


    @PostMapping("/register/{userId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<AttendeeResponse.Simple>> registerUser(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        AttendeeResponse.Simple attendee = attendeeService.registerUser(
                eventId, userId, currentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<AttendeeResponse.Simple>builder()
                        .message("User registered for the event")
                        .data(attendee)
                        .build()
        );
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<List<AttendeeResponse.Summary>>> getAttendees(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "registeredAt") Pageable pageable) {

        Page<AttendeeResponse.Summary> page = attendeeService.getAttendees(
                eventId, status, currentUser.id(), pageable);

        return ResponseEntity.ok(
                ApiResponse.Success.<List<AttendeeResponse.Summary>>builder()
                        .data(page.getContent())
                        .meta(ApiResponse.Meta.builder()
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .build())
                        .build()
        );
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse.Success<AttendeeResponse.Simple>> getMyRegistration(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        AttendeeResponse.Simple attendee = attendeeService.getMyRegistration(
                eventId, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<AttendeeResponse.Simple>builder()
                        .data(attendee)
                        .build()
        );
    }

    @DeleteMapping("/{attendeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse.Success<AttendeeResponse.Simple>> cancelRegistration(
            @PathVariable Long eventId,
            @PathVariable Long attendeeId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        AttendeeResponse.Simple attendee = attendeeService.cancelRegistration(
                eventId, attendeeId, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<AttendeeResponse.Simple>builder()
                        .message("Registration cancelled")
                        .data(attendee)
                        .build()
        );
    }
}
