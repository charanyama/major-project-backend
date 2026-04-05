package com.virtualstore.user_service.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * User Entity for the ecommerce application
 * Represents a user account with authentication and profile information
 */

@Document(collation = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Phone No. is required")
    private String phone;

    @NotBlank(message = "Password is required")
    private String passwordHash;

    private Role role;

    private Status status;

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean phoneVerified = false;

    private Instant lastLoginAt;

    private String verificationToken;
    private Instant verificationTokenExpiry;

    private String passwordResetToken;
    private Instant passwordResetTokenExpiry;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
}
