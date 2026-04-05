package com.virtualstore.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MessageResponse
 *
 * Generic wrapper for endpoints that return a plain message
 * rather than data (e.g. signout, resend-verification, forgot-password).
 */
@Getter
@AllArgsConstructor
public class MessageResponse {
    private String message;
}