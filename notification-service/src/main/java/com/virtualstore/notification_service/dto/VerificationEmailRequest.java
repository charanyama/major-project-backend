package com.virtualstore.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload used when the user-service asks notification-service to
 * send a verification email during onboarding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationEmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be valid")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Verification URL is required")
    private String verificationUrl;

    @Min(value = 1, message = "Expiry hours must be at least 1")
    private Integer expiryHours;
}
