package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.SessionRequest;
import com.api.springcore.dto.SessionResponse;
import com.api.springcore.security.CustomUserDetailsService;
import com.api.springcore.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events/{eventId}/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<SessionResponse.Simple>> createSession(
            @PathVariable Long eventId,
            @Valid @RequestBody SessionRequest.Create request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        SessionResponse.Simple session = sessionService.createSession(eventId, request, currentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<SessionResponse.Simple>builder()
                        .message("Session created")
                        .data(session)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse.Success<List<SessionResponse.Simple>>> getSessions(
            @PathVariable Long eventId) {

        List<SessionResponse.Simple> sessions = sessionService.getSessionsByEvent(eventId);
        return ResponseEntity.ok(
                ApiResponse.Success.<List<SessionResponse.Simple>>builder()
                        .data(sessions)
                        .build()
        );
    }



    @GetMapping("/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse.Success<SessionResponse.Detail>> getSession(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        SessionResponse.Detail session = sessionService.getSession(eventId, sessionId, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<SessionResponse.Detail>builder()
                        .data(session)
                        .build()
        );
    }

    @PatchMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<SessionResponse.Simple>> updateSession(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @RequestBody SessionRequest.Update request,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        SessionResponse.Simple session = sessionService.updateSession(
                eventId, sessionId, request, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<SessionResponse.Simple>builder()
                        .message("Session updated")
                        .data(session)
                        .build()
        );
    }



    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<Void>> deleteSession(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        sessionService.deleteSession(eventId, sessionId, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<Void>builder()
                        .message("Session deleted")
                        .build()
        );
    }


    @PostMapping("/{sessionId}/qr")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<SessionResponse.Detail>> generateQr(
            @PathVariable Long eventId,
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        SessionResponse.Detail session = sessionService.generateSessionQr(
                eventId, sessionId, currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<SessionResponse.Detail>builder()
                        .message("Session QR generated")
                        .data(session)
                        .build()
        );
    }
}