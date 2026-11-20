package com.hireai.auth.controller;

import com.hireai.auth.dto.*;
import com.hireai.auth.security.UserPrincipal;
import com.hireai.auth.service.AuthService;
import com.hireai.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserSummaryDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserSummaryDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Account registered successfully. A 6-digit verification PIN has been sent to your work email.", user)
        );
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyPin(@Valid @RequestBody VerifyPinRequest request) {
        AuthResponse response = authService.verifyPin(request);
        return ResponseEntity.ok(ApiResponse.success("Work email verified successfully. Welcome to HireAI!", response));
    }

    @PostMapping("/resend-pin")
    public ResponseEntity<ApiResponse<Void>> resendPin(@Valid @RequestBody ResendPinRequest request) {
        authService.resendPin(request);
        return ResponseEntity.ok(ApiResponse.success("A new 6-digit verification PIN has been dispatched to your work email."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully.", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserSummaryDto user = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully.", user));
    }
}
