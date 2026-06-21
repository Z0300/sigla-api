package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.StatsResponse;
import com.api.springcore.security.CustomUserDetailsService;
import com.api.springcore.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/organizer/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse.Success<StatsResponse.OrganizerStats>> getStats(
            @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal currentUser) {

        StatsResponse.OrganizerStats stats = statsService.getOrganizerStats(currentUser.id());
        return ResponseEntity.ok(
                ApiResponse.Success.<StatsResponse.OrganizerStats>builder()
                        .data(stats)
                        .build()
        );
    }
}