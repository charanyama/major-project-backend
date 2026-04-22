package com.virtualstore.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.virtualstore.notification_service.dto.NotificationRequest;
import com.virtualstore.notification_service.dto.NotificationResponse;
import com.virtualstore.notification_service.dto.VerificationEmailRequest;
import com.virtualstore.notification_service.entity.NotificationLog;
import com.virtualstore.notification_service.entity.NotificationStatus;
import com.virtualstore.notification_service.entity.NotificationType;
import com.virtualstore.notification_service.repository.NotificationLogRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * NotificationService
 *
 * Orchestrates the full notification lifecycle:
 * 1. Validate the inbound request
 * 2. Persist a PENDING log entry
 * 3. Dispatch to the appropriate channel (Email / SMS)
 * 4. Update the log entry to SENT or FAILED
 * 5. Return a NotificationResponse to the caller
 *
 * The send call blocks until the configured provider (SMTP) accepts the
 * message, so the HTTP response accurately reflects delivery success/
 * failure when the controller returns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository logRepository;
    private final EmailDispatchService emailDispatchService;
    // private final SmsDispatchService smsDispatchService;

    // Subject lines for email templates
    private static final Map<String, String> EMAIL_SUBJECTS = Map.of(
            "verify-email", "Verify your email address",
            "reset-password", "Reset your password");

    /**
     * Entry point — validates, logs, dispatches, and returns outcome.
     */
    public NotificationResponse process(NotificationRequest request) {
        validate(request);

        // Persist PENDING log
        NotificationLog logEntry = NotificationLog.builder()
                .type(NotificationType.valueOf(request.getType().name()))
                .template(request.getTemplate())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .payload(maskSensitiveFields(request.getPayload()))
                .status(NotificationStatus.PENDING)
                .attempts(1)
                .build();
        logEntry = logRepository.save(logEntry);

        // Dispatch asynchronously and update log after send
        try {
            String providerId = dispatch(request);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setProviderMessageId(providerId);
            logRepository.save(logEntry);

            return NotificationResponse.builder()
                    .success(true)
                    .notificationId(logEntry.getId().toString())
                    .message("Notification dispatched successfully")
                    .build();

        } catch (Exception ex) {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(ex.getMessage());
            logRepository.save(logEntry);

            log.error("[NotificationService] Dispatch failed for log id={}: {}",
                    logEntry.getId(), ex.getMessage());

            return NotificationResponse.builder()
                    .success(false)
                    .notificationId(logEntry.getId().toString())
                    .message("Dispatch failed: " + ex.getMessage())
                    .build();
        }
    }

    // ------------------------------------------------------------------ //
    // Private helpers
    // ------------------------------------------------------------------ //

    private String dispatch(NotificationRequest request) {
        return switch (request.getType()) {
            case EMAIL -> {
                String subject = EMAIL_SUBJECTS.getOrDefault(
                        request.getTemplate(), "Notification from VirtualStore");

                yield emailDispatchService.send(
                        request.getRecipientEmail(),
                        subject,
                        request.getTemplate(),
                        request.getPayload());
            }
            case SMS -> {
                yield "SMS not implemented yet";
            }
        };
    }

    /**
     * Helper for common verification emails during user onboarding.
     */
    public NotificationResponse sendVerificationEmail(VerificationEmailRequest verificationRequest) {
        log.info("[NotificationService] Sending verification email to {}", verificationRequest.getEmail());

        int expiryHours = Optional.ofNullable(verificationRequest.getExpiryHours()).orElse(24);
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", verificationRequest.getFirstName());
        payload.put("verifyUrl", verificationRequest.getVerificationUrl());
        payload.put("expiryHours", expiryHours);

        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .template("verify-email")
                .recipientEmail(verificationRequest.getEmail())
                .payload(payload)
                .build();

        return process(request);
    }

    private void validate(NotificationRequest req) {
        if (req.getType() == null) {
            throw new IllegalArgumentException("Notification type must not be null");
        }
        switch (req.getType()) {
            case EMAIL -> {
                if (req.getRecipientEmail() == null || req.getRecipientEmail().isBlank())
                    throw new IllegalArgumentException("recipientEmail is required for EMAIL notifications");
            }
            case SMS -> {
                if (req.getRecipientPhone() == null || req.getRecipientPhone().isBlank())
                    throw new IllegalArgumentException("recipientPhone is required for SMS notifications");
            }
        }
    }

    /**
     * Masks sensitive values before persisting to the log.
     * Tokens and OTPs are replaced with "***".
     */
    private Map<String, Object> maskSensitiveFields(Map<String, Object> payload) {
        if (payload == null)
            return null;
        var masked = new java.util.HashMap<>(payload);
        masked.replaceAll((k, v) -> {
            if (k.toLowerCase().contains("token") || k.toLowerCase().contains("otp"))
                return "***";
            return v;
        });
        return masked;
    }
}
