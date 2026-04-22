package com.virtualstore.notification_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.virtualstore.notification_service.dto.NotificationRequest;
import com.virtualstore.notification_service.dto.NotificationResponse;
import com.virtualstore.notification_service.dto.VerificationEmailRequest;
import com.virtualstore.notification_service.service.NotificationService;

/**
 * NotificationController
 *
 * Single endpoint consumed by upstream services (user-service, order-service,
 * etc.)
 * POST /api/v1/notifications/send
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    /**
     * Send a notification.
     *
     * Returns 200 OK regardless of delivery success so callers can
     * log-and-continue.
     * The `success` field in the body indicates actual delivery status.
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody NotificationRequest request) {

        log.info("[Controller] Received notification request: type={}, template={}, recipient={}",
                request.getType(),
                request.getTemplate(),
                request.getRecipientEmail() != null
                        ? request.getRecipientEmail()
                        : request.getRecipientPhone());

        NotificationResponse response = notificationService.process(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<NotificationResponse> sendVerificationEmail(
            @Valid @RequestBody VerificationEmailRequest request) {

        log.info("[Controller] Received verify-email request for {}", request.getEmail());
        NotificationResponse response = notificationService.sendVerificationEmail(request);
        return ResponseEntity.ok(response);
    }
}
