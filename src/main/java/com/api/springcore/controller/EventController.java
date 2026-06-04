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
    public ResponseEntity<ApiResponse.Success<List<EventResponse.toDto>>> listPermissions(
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

    @PostMapping
    @PreAuthorize("hasAuthority('events:create')")
    @Operation(summary = "Create a new event")
    public ResponseEntity<ApiResponse.Success<EventResponse.CreateDto>> createPermission(
            @Valid @RequestBody EventRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<EventResponse.CreateDto>builder()
                        .message("Event created")
                        .data(eventService.create(request))
                        .build());
    }
}
