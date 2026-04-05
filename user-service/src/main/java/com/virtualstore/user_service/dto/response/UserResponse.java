package com.virtualstore.user_service.dto.response;

import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.entity.User;
import com.virtualstore.user_service.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * UserResponse
 *
 * Safe public projection of the User entity.
 * Never exposes passwordHash, verification tokens, or reset tokens.
 */
@Getter
@Builder
public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private Role role;
    private Status status;
    private boolean emailVerified;
    private String phone;
    private boolean phoneVerified;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Maps a User entity to a UserResponse — keeps mapping logic off the controller
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .phone(user.getPhone())
                .phoneVerified(user.isPhoneVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}