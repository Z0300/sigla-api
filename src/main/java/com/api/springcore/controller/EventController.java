package com.api.springcore.controller;

import com.api.springcore.dto.*;
import com.api.springcore.security.CustomUserDetailsService;
import com.api.springcore.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Events management endpoints")
public class EventController {

    private final EventService eventService;

    @GetMapping("/public")
    @PreAuthorize("hasAuthority('events:read')")
    @Operation(summary = "List all events")
    public ResponseEntity<ApiResponse.Success<List<EventResponse.toPublicDto>>> publicEvents(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EventResponse.toPublicDto> page = eventService.getPublicEvents(searchTerm, status, pageable);
        return ResponseEntity.ok(ApiResponse.Success.<List<EventResponse.toPublicDto>>builder()
                .data(page.getContent())
                .meta(ApiResponse.Meta.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('events:read')")
    @Operation(summary = "List all events")
    public ResponseEntity<ApiResponse.Success<List<EventResponse.toPublicDto>>> getOrganizerEvents(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal
    ) {
        Page<EventResponse.toPublicDto> page = eventService.getEvents(principal.id(), searchTerm, status, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.Success.<List<EventResponse.toPublicDto>>builder()
                .data(page.getContent())
                .meta(ApiResponse.Meta.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('events:read')")
    @Operation(summary = "Get an event by ID")
    public ResponseEntity<ApiResponse.Success<EventResponse.toDto>> getEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<EventResponse.toDto>builder()
                .data(eventService.getEvent(id))
                .build());
    }

    @GetMapping("/withSession/{id}")
    @PreAuthorize("hasAuthority('events:read')")
    @Operation(summary = "Get an event by ID")
    public ResponseEntity<ApiResponse.Success<EventResponse.toPublicSessionDto>> getEventWithSessions(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<EventResponse.toPublicSessionDto>builder()
                .data(eventService.getEventWithSession(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('events:create')")
    @Operation(summary = "Create a new event")
    public ResponseEntity<ApiResponse.Success<EventResponse.toSimpleDto>> createEvent(
            @Valid @RequestBody EventRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<EventResponse.toSimpleDto>builder()
                        .message("Event created")
                        .data(eventService.create(request))
                        .build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('events:update')")
    @Operation(summary = "Update an event")
    public ResponseEntity<ApiResponse.Success<EventResponse.toSimpleDto>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest.Update request) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.Success.<EventResponse.toSimpleDto>builder()
                        .message("Event updated")
                        .data(eventService.updateEvent(id, request))
                        .build());
    }

    @PatchMapping("/{id}/{newStatus}")
    @PreAuthorize("hasAuthority('events:update')")
    @Operation(summary = "Update event status")
    public ResponseEntity<ApiResponse.Success<EventResponse.toSimpleDto>> updateEventStatus(
            @PathVariable Long id,
            @PathVariable String newStatus) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.Success.<EventResponse.toSimpleDto>builder()
                        .message("Event updated")
                        .data(eventService.transitionStatus(id, newStatus))
                        .build());
    }

}
