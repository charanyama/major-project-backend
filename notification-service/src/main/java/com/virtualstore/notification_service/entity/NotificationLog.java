package com.virtualstore.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * NotificationLog
 *
 * Persists every notification attempt — one row per send call.
 * Tracks the channel (EMAIL / SMS), template used, recipient,
 * delivery status, and any error message for observability.
 */
@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_notification_recipient_email", columnList = "recipient_email"),
    @Index(name = "idx_notification_recipient_phone", columnList = "recipient_phone"),
    @Index(name = "idx_notification_status",         columnList = "status"),
    @Index(name = "idx_notification_created_at",     columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationType type;                  // EMAIL | SMS

    @Column(nullable = false, length = 100)
    private String template;                        // e.g. "verify-email"

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    /**
     * Arbitrary template variables stored as JSONB.
     * Sensitive values (tokens, OTPs) should be masked before storage.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /** Provider message-id returned by SMTP server or Twilio SID */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Number of delivery attempts (incremented on each retry if added later) */
    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
