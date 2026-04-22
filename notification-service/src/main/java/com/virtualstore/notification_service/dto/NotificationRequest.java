package com.virtualstore.notification_service.dto;

import com.virtualstore.notification_service.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Inbound payload received from any upstream service (e.g. user-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Template name is required")
    private String template;

    /** Required when type = EMAIL */
    private String recipientEmail;

    /** Required when type = SMS */
    private String recipientPhone;

    /** Template variables injected at render time */
    private Map<String, Object> payload;
}