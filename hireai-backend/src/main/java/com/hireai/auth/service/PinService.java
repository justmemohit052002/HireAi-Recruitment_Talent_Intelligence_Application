package com.hireai.auth.service;

import com.hireai.auth.entity.EmailVerification;
import com.hireai.auth.entity.User;

public interface PinService {

    /**
     * Generates a 6-digit PIN, stores its SHA-256 hash, and returns the plaintext PIN
     * for single-dispatch transmission via email.
     */
    String generateAndSavePin(User user);

    /**
     * Verifies a 6-digit PIN against the user's latest active verification record.
     */
    boolean verifyPin(User user, String rawPin);

    /**
     * Computes the SHA-256 hash of a string.
     */
    String hashPin(String rawPin);
}
