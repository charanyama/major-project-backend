package com.virtualstore.user_service.event;

public record UserVerificationRequestedEvent(
        String email,
        String fullName,
        String verificationToken) {
}
