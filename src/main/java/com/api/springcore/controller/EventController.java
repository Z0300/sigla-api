package com.api.springcore.controller;

import com.api.springcore.dto.*;
import com.api.springcore.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Events management endpoints")
public class EventController {

    private final EventService eventService;

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
