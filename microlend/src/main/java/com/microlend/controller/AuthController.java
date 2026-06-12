package com.microlend.controller;

import com.microlend.dto.request.LoginRequest;
import com.microlend.dto.request.RegisterUserRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.dto.response.AuthResponse;
import com.microlend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController
 *
 * BUG FIX #1: POST /api/auth/register is NO LONGER a public endpoint.
 * It now requires the caller to be authenticated as ADMIN or BRANCH_MANAGER.
 * Anonymous or low-privilege callers receive 401/403 instead of being
 * able to create arbitrary privileged accounts.
 *
 * BUG FIX #2: BRANCH_MANAGER is now an allowed registrar. Scope enforcement
 * (branch restriction + role restriction) is handled inside AuthService.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login — Public endpoint, returns JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/auth/register — PROTECTED: requires ADMIN or BRANCH_MANAGER.
     *
     * BUG FIX #1: Removed from permitAll(). Now requires authentication.
     * BUG FIX #2: BRANCH_MANAGER may register staff within their own branch only
     *             (scope enforced in AuthService.register()).
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterUserRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
