package com.virtualstore.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private boolean success;
    private String notificationId;  // UUID of the persisted NotificationLog row
    private String message;
}