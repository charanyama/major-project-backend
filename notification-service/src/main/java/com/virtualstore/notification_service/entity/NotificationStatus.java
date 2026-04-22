package com.virtualstore.notification_service.entity;

public enum NotificationStatus {
    PENDING, // received, not yet dispatched
    SENT, // successfully handed off to provider
    FAILED // provider rejected or unreachable
}