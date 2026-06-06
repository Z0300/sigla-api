package com.api.springcore.controller;

import com.api.springcore.dto.*;
import com.api.springcore.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Events management endpoints")
public class EventController {

    private final EventService eventService;


    @GetMapping
    @PreAuthorize("hasAuthority('events:read')")
    @Operation(summary = "List all events")
    public ResponseEntity<ApiResponse.Success<List<EventResponse.toDto>>> listEvents(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EventResponse.toDto> page = eventService.getEvents(searchTerm, status, pageable);
        return ResponseEntity.ok(ApiResponse.Success.<List<EventResponse.toDto>>builder()
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
    public ResponseEntity<ApiResponse.Success<EventResponse.toDto>> listEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.Success.<EventResponse.toDto>builder()
                .data(eventService.getPermission(id))
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
