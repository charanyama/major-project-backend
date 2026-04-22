package com.virtualstore.user_service.dto.response;
import com.virtualstore.user_service.entity.User;

import lombok.Builder;
import lombok.Getter;

/**
 * AuthResponse
 *
 * Returned on successful login or token refresh.
 * Contains both tokens and basic user info so the frontend
 * doesn't need a separate /me call immediately after login.
 */
@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private User user;
}