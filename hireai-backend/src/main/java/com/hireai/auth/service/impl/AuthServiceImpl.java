package com.hireai.auth.service.impl;

import com.hireai.auth.dto.*;
import com.hireai.auth.entity.Organization;
import com.hireai.auth.entity.Role;
import com.hireai.auth.entity.User;
import com.hireai.auth.enums.AccountStatus;
import com.hireai.auth.enums.RoleType;
import com.hireai.auth.repository.OrganizationRepository;
import com.hireai.auth.repository.RoleRepository;
import com.hireai.auth.repository.UserRepository;
import com.hireai.auth.security.JwtService;
import com.hireai.auth.service.AuthService;
import com.hireai.auth.service.EmailNotificationService;
import com.hireai.auth.service.PinService;
import com.hireai.common.exception.AuthenticationException;
import com.hireai.common.exception.BadRequestException;
import com.hireai.common.exception.ResourceNotFoundException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PinService pinService;
    private final EmailNotificationService emailNotificationService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserSummaryDto register(RegisterRequest request) {
        String cleanEmail = request.getWorkEmail().trim().toLowerCase();

        if (userRepository.existsByWorkEmailIgnoreCase(cleanEmail)) {
            throw new BadRequestException("An account with work email " + cleanEmail + " already exists.");
        }

        // Find existing organization or create a new one
        String orgName = request.getOrganizationName().trim();
        Organization organization = organizationRepository.findByNameIgnoreCase(orgName)
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name(orgName)
                        .build()));

        // Assign default ORG_ADMIN role to the registering user
        Role adminRole = roleRepository.findById(RoleType.ROLE_ORG_ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .id(RoleType.ROLE_ORG_ADMIN.name())
                        .name("Organization Admin")
                        .description("Organization administrator")
                        .build()));

        User user = User.builder()
                .organization(organization)
                .fullName(request.getFullName().trim())
                .workEmail(cleanEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .roles(new HashSet<>(Collections.singletonList(adminRole)))
                .build();

        user = userRepository.save(user);

        // Generate and dispatch 6-digit verification PIN
        String rawPin = pinService.generateAndSavePin(user);
        emailNotificationService.sendVerificationPin(user.getWorkEmail(), user.getFullName(), rawPin);

        return mapToSummary(user);
    }

    @Override
    @Transactional
    public AuthResponse verifyPin(VerifyPinRequest request) {
        String cleanEmail = request.getWorkEmail().trim().toLowerCase();
        User user = userRepository.findByWorkEmailWithDetails(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with work email: " + cleanEmail));

        if (user.isEmailVerified() && user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Work email is already verified. Please sign in.");
        }

        // Verify PIN hash, expiry, and attempt limits
        pinService.verifyPin(user, request.getPin().trim());

        // Mark account active
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        log.info("User {} successfully verified their email address and activated account", cleanEmail);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(mapToSummary(user))
                .build();
    }

    @Override
    @Transactional
    public void resendPin(ResendPinRequest request) {
        String cleanEmail = request.getWorkEmail().trim().toLowerCase();
        User user = userRepository.findByWorkEmailWithDetails(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with work email: " + cleanEmail));

        if (user.isEmailVerified() && user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("This account is already verified. Please log in.");
        }

        String rawPin = pinService.generateAndSavePin(user);
        emailNotificationService.sendVerificationPin(user.getWorkEmail(), user.getFullName(), rawPin);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getWorkEmail().trim().toLowerCase();
        User user = userRepository.findByWorkEmailWithDetails(cleanEmail)
                .orElseThrow(() -> new AuthenticationException("Invalid work email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid work email or password.");
        }

        if (!user.isEmailVerified() || user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new AuthenticationException("Email is not verified. Please verify your 6-digit PIN before signing in.");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new AuthenticationException("Your account has been suspended. Please contact your organization administrator.");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(mapToSummary(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token.");
        }

        Claims claims = jwtService.extractClaims(refreshToken);
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new AuthenticationException("Provided token is not a refresh token.");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new AuthenticationException("User account not found."));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(mapToSummary(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToSummary(user);
    }

    private UserSummaryDto mapToSummary(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        UUID orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        String orgName = user.getOrganization() != null ? user.getOrganization().getName() : null;

        return UserSummaryDto.builder()
                .id(user.getId())
                .organizationId(orgId)
                .organizationName(orgName)
                .fullName(user.getFullName())
                .workEmail(user.getWorkEmail())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus())
                .roles(roleNames)
                .build();
    }
}
