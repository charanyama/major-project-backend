package com.virtualstore.user_service.dto.response;

import com.virtualstore.user_service.entity.Role;
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

    /** Milliseconds until the access token expires */
    private long accessTokenExpiresIn;

    private String id;
    private String email;
    private String fullName;
    private Role role;
}