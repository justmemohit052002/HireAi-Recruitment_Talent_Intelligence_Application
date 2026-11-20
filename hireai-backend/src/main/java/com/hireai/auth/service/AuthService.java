package com.hireai.auth.service;

import com.hireai.auth.dto.*;

import java.util.UUID;

public interface AuthService {

    UserSummaryDto register(RegisterRequest request);

    AuthResponse verifyPin(VerifyPinRequest request);

    void resendPin(ResendPinRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserSummaryDto getCurrentUser(UUID userId);
}
