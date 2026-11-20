package com.hireai.auth.service.impl;

import com.hireai.auth.entity.EmailVerification;
import com.hireai.auth.entity.User;
import com.hireai.auth.repository.EmailVerificationRepository;
import com.hireai.auth.service.PinService;
import com.hireai.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PinServiceImpl implements PinService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${hireai.security.pin.expiration-minutes:10}")
    private int pinExpirationMinutes;

    @Value("${hireai.security.pin.max-attempts:5}")
    private int pinMaxAttempts;

    @Override
    @Transactional
    public String generateAndSavePin(User user) {
        // Generate a cryptographically secure 6-digit numeric PIN
        int pinNumber = secureRandom.nextInt(1_000_000);
        String rawPin = String.format("%06d", pinNumber);

        String pinHash = hashPin(rawPin);
        Instant expiresAt = Instant.now().plus(pinExpirationMinutes, ChronoUnit.MINUTES);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .pinHash(pinHash)
                .expiresAt(expiresAt)
                .attempts(0)
                .build();

        emailVerificationRepository.save(verification);
        log.debug("Generated 6-digit verification PIN for user {}", user.getWorkEmail());

        return rawPin;
    }

    @Override
    @Transactional
    public boolean verifyPin(User user, String rawPin) {
        EmailVerification verification = emailVerificationRepository.findLatestActiveByUser(user)
                .orElseThrow(() -> new BadRequestException("No active verification PIN found for this account. Please request a new one."));

        if (verification.isConsumed()) {
            throw new BadRequestException("This verification PIN has already been used. Please request a new one.");
        }

        if (verification.isExpired()) {
            throw new BadRequestException("Verification PIN has expired. Please request a new PIN.");
        }

        if (verification.getAttempts() >= pinMaxAttempts) {
            throw new BadRequestException("Maximum verification attempts reached. Please request a new PIN.");
        }

        // Increment attempts on each check
        verification.setAttempts(verification.getAttempts() + 1);

        String inputHash = hashPin(rawPin);
        if (MessageDigest.isEqual(verification.getPinHash().getBytes(StandardCharsets.UTF_8), inputHash.getBytes(StandardCharsets.UTF_8))) {
            verification.setConsumedAt(Instant.now());
            emailVerificationRepository.save(verification);
            return true;
        }

        emailVerificationRepository.save(verification);
        int remainingAttempts = pinMaxAttempts - verification.getAttempts();
        if (remainingAttempts > 0) {
            throw new BadRequestException("Invalid PIN. " + remainingAttempts + " attempts remaining.");
        } else {
            throw new BadRequestException("Invalid PIN. Maximum attempts reached. Please request a new PIN.");
        }
    }

    @Override
    public String hashPin(String rawPin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPin.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
