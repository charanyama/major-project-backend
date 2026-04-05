package com.virtualstore.user_service.service;

import com.virtualstore.user_service.dto.request.ChangePasswordRequest;
import com.virtualstore.user_service.dto.request.UserRequest;
import com.virtualstore.user_service.dto.response.UserResponse;
import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.entity.Status;
import com.virtualstore.user_service.entity.User;
import com.virtualstore.user_service.exception.AppException;
import com.virtualstore.user_service.exception.EmailAlreadyExistsException;
import com.virtualstore.user_service.exception.UserNotFoundException;
import com.virtualstore.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

/**
 * UserService
 *
 * Handles all user CRUD and lifecycle operations.
 * Called by both UserController (admin ops) and AuthController (self-service
 * ops).
 *
 * Methods map directly to UserController endpoints:
 *
 * createUser(UserRequest) POST /api/v1/users
 * getUserById(id) GET /api/v1/users/{id}
 * getUserByEmail(email) GET /api/v1/users/email/{email}
 * getAllUsers() GET /api/v1/users
 * getUsersByRole(role) GET /api/v1/users/role/{role}
 * enableUser(id) GET /api/v1/users/status/{id}
 * updateUser(id, UserRequest) PUT /api/v1/users/{id}
 * deactivateUser(id) PATCH /api/v1/users/{id}/deactivate
 * verifyEmail(id) PATCH /api/v1/users/{id}/verify-email
 * verifyPhone(id) PATCH /api/v1/users/{id}/verify-phone
 * updateLastLogin(id) PATCH /api/v1/users/{id}/last-login
 * deleteUser(id) DELETE /api/v1/users/{id}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    /**
     * Admin-only: creates a user directly, bypassing the email verification flow.
     * Account is immediately ACTIVE — admin-created accounts are pre-trusted.
     */
    public UserResponse createUser(UserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(req.getEmail());
        }

        if (!StringUtils.hasText(req.getPassword())) {
            throw new AppException("Password is required when creating a user.", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .email(req.getEmail())
                .fullName(req.getFullName())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .phone(req.getPhone())
                .enabled(true)
                .emailVerified(true)
                .build();

        userRepository.save(user);
        log.info("Admin created user: {} with role {}", user.getEmail(), user.getRole());
        return UserResponse.from(user);
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public UserResponse getUserById(String id) {
        return UserResponse.from(findActiveUser(id));
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException(email));
        return UserResponse.from(user);
    }

    /** Returns all non-deleted users. Admin only. */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByDeletedFalse()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    /** Returns all non-deleted users with the given role. Admin only. */
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAllByRoleAndDeletedFalse(role)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * Enables a user account — sets status ACTIVE, enabled=true.
     * Mapped to GET /api/v1/users/status/{id} per the controller.
     */
    public UserResponse enableUser(String id) {
        User user = findActiveUser(id);
        if (user.getStatus() == Status.ACTIVE) {
            return UserResponse.from(user);
        }
        user.setStatus(Status.ACTIVE);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled: {}", id);
        return UserResponse.from(user);
    }

    // -------------------------------------------------------------------------
    // UPDATE — Admin
    // -------------------------------------------------------------------------

    /**
     * Full admin update of a user.
     * Password is re-hashed only if a new one is provided in the request.
     * Email uniqueness is re-validated only if the email is changing.
     */
    public UserResponse updateUser(String id, UserRequest req) {
        User user = findActiveUser(id);

        if (!user.getEmail().equalsIgnoreCase(req.getEmail()) &&
                userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(req.getEmail());
        }

        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setPhone(req.getPhone());

        if (StringUtils.hasText(req.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);
        log.info("Admin updated user: {}", id);
        return UserResponse.from(user);
    }

    /**
     * Deactivates a user — INACTIVE status, enabled=false.
     * Account is retained and can be re-enabled by admin.
     */
    public UserResponse deactivateUser(String id) {
        User user = findActiveUser(id);
        if (user.getStatus() == Status.INACTIVE) {
            return UserResponse.from(user);
        }
        user.setStatus(Status.INACTIVE);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User deactivated: {}", id);
        return UserResponse.from(user);
    }

    /**
     * Marks the user's email as verified and promotes status PENDING -> ACTIVE.
     */
    public UserResponse verifyEmail(String id) {
        User user = findActiveUser(id);
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        if (user.getStatus() == Status.PENDING) {
            user.setStatus(Status.ACTIVE);
            user.setEnabled(true);
        }

        userRepository.save(user);
        log.info("Email verified for user: {}", id);
        return UserResponse.from(user);
    }

    /**
     * Marks the phone number as verified.
     * Requires a phone number to already be on file.
     */
    public UserResponse verifyPhone(String id) {
        User user = findActiveUser(id);

        if (!StringUtils.hasText(user.getPhone())) {
            throw new AppException(
                    "No phone number on file. Add a phone number before verifying.",
                    HttpStatus.BAD_REQUEST);
        }

        user.setPhoneVerified(true);
        userRepository.save(user);
        log.info("Phone verified for user: {}", id);
        return UserResponse.from(user);
    }

    /**
     * Records the current timestamp as the user's last successful login.
     * Called by AuthService after every successful authentication.
     */
    public void updateLastLogin(String id) {
        User user = findActiveUser(id);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        log.debug("Last login updated for user: {}", id);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    /**
     * Soft-deletes a user: deleted=true, status=DELETED, enabled=false.
     * Data is retained for audit. A scheduled job can purge after retention period.
     */
    public void deleteUser(String id) {
        User user = findActiveUser(id);
        user.setDeleted(true);
        user.setStatus(Status.DELETED);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User soft-deleted: {}", id);
    }

    // -------------------------------------------------------------------------
    // Self-service (called via AuthController / profile endpoints)
    // -------------------------------------------------------------------------

    public UserResponse getProfile(String userId) {
        return UserResponse.from(findActiveUser(userId));
    }

    public void changePassword(String userId, ChangePasswordRequest req) {
        User user = findActiveUser(userId);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException("Current password is incorrect.", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new AppException(
                    "New password must be different from the current password.",
                    HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for userId: {}", userId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User findActiveUser(String id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}