package com.virtualstore.user_service.dto.request;

import com.virtualstore.user_service.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Full Name name is required")
    @Size(min = 2, max = 50)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain uppercase, lowercase, digit, and special character")
    private String password;

    /**
     * The role this user is signing up as.
     * Must be one of: USER, SELLER, ADMIN.
     * Policy: one user, one role — enforced here at the request boundary.
     *
     * Note: in a real system you would likely restrict ADMIN signup
     * to an internal flow. Consider validating this in AuthService
     * (e.g. reject ADMIN self-registration).
     */
    @NotNull(message = "Role is required")
    private Role role;
}