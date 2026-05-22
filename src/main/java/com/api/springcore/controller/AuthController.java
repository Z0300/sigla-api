package com.api.springcore.controller;

import com.api.springcore.dto.ApiResponse;
import com.api.springcore.dto.AuthRequest;
import com.api.springcore.dto.DomainResponse;
import com.api.springcore.exception.UnauthorizedException;
import com.api.springcore.security.CustomUserDetailsService.UserPrincipal;
import com.api.springcore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse.Success<DomainResponse.TokenDto>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        DomainResponse.TokenDto tokens = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.Success.<DomainResponse.TokenDto>builder()
                        .message("Registration successful")
                        .data(tokens)
                        .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse.Success<DomainResponse.TokenDto>> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpServletResponse response) {
        DomainResponse.TokenDto tokens = authService.login(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        tokens.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.TokenDto>builder()
                .message("Login successful")
                .data(tokens)
                .build());
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse.Success<DomainResponse.TokenDto>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            throw new UnauthorizedException("No refresh token provided");
        }

        DomainResponse.TokenDto tokens = authService.refreshToken(refreshToken);

        ResponseCookie newCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
        tokens.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.Success.<DomainResponse.TokenDto>builder()
                .message("Token refreshed")
                .data(tokens)
                .build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke all refresh tokens for the authenticated user")
    public ResponseEntity<ApiResponse.Success<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response) {
        authService.logout(principal.id());

        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(ApiResponse.Success.<Void>builder()
                .message("Logged out successfully")
                .build());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the authenticated user")
    public ResponseEntity<ApiResponse.Success<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AuthRequest.ChangePassword request) {
        authService.changePassword(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.Success.<Void>builder()
                .message("Password changed successfully")
                .build());
    }
}