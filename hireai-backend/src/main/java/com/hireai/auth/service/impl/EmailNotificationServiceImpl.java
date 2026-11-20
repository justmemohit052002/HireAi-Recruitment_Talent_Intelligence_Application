package com.hireai.auth.service.impl;

import com.hireai.auth.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendVerificationPin(String toEmail, String fullName, String rawPin) {
        log.info("================================================================================");
        log.info("✉️  [EMAIL VERIFICATION PIN] Sent to: {}", toEmail);
        log.info("    Recipient: {}", fullName);
        log.info("    6-Digit Verification PIN: {}", rawPin);
        log.info("    Valid for 10 minutes (maximum 5 attempts).");
        log.info("================================================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("HireAI - Verify Your Work Email");
            message.setText("Hello " + fullName + ",\n\n" +
                    "Welcome to HireAI Talent Intelligence Platform.\n\n" +
                    "Your 6-digit verification PIN is: " + rawPin + "\n\n" +
                    "This PIN will expire in 10 minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Best regards,\nThe HireAI Team");

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Could not dispatch email via SMTP (development mode active). PIN is logged above. Error: {}", e.getMessage());
        }
    }
}
