package com.virtualstore.user_service.dto.request;

import com.virtualstore.user_service.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * UserRequest
 *
 * Used by ADMIN endpoints to create or fully update a user.
 * Unlike SignupRequest (self-registration), this allows setting
 * any role including ADMIN, and optionally a phone number.
 *
 * POST /api/v1/users — create user (admin)
 * PUT /api/v1/users/{id} — update user (admin)
 */
@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String fullName;


    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    /**
     * Password is required on create (POST) but optional on update (PUT).
     * UserService checks: if blank on update, keep the existing hash.
     */
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain uppercase, lowercase, digit, and special character")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format")
    private String phone;
}