package com.hireai.auth.service;

public interface EmailNotificationService {
    void sendVerificationPin(String toEmail, String fullName, String rawPin);
}
