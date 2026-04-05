package com.virtualstore.user_service.controller;

import com.virtualstore.user_service.dto.request.*;
import com.virtualstore.user_service.dto.response.AuthResponse;
import com.virtualstore.user_service.dto.response.MessageResponse;
import com.virtualstore.user_service.dto.response.UserResponse;
import com.virtualstore.user_service.security.UserPrincipal;
import com.virtualstore.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * Handles all authentication and account lifecycle endpoints.
 *
 * Public (no JWT required):
 * POST /auth/signup
 * POST /auth/login
 * GET /auth/verify-email?token=
 * POST /auth/resend-verification
 * POST /auth/forgot-password
 * POST /auth/reset-password
 *
 * Protected (JWT required):
 * GET /auth/me
 * POST /auth/refresh
 * POST /auth/signout
 * DELETE /auth/account
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Public endpoints ──────────────────────────────────────────────────────

    /**
     * POST /auth/signup
     *
     * Registers a new user. Account is inactive until email is verified.
     * Body: { firstName, lastName, email, password, role }
     * Role must be one of: USER, SELLER (ADMIN is rejected)
     */
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(
            @Valid @RequestBody SignupRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(req));
    }

    /**
     * POST /auth/login
     *
     * Authenticates credentials and returns access + refresh tokens.
     * Body: { email, password }
     * Returns 401 if credentials wrong, 403 if email not verified.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {

        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * GET /auth/verify-email?token={token}
     *
     * Activates the account linked to the verification token.
     * The token is sent to the user's email after signup.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @RequestParam String token) {

        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    /**
     * POST /auth/resend-verification
     *
     * Re-sends the verification email if the original link expired.
     * Body: { email }
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ForgotPasswordRequest req) {

        // Reuses ForgotPasswordRequest — both just need an email field
        return ResponseEntity.ok(
                authService.resendVerification(req.getEmail()));
    }

    /**
     * POST /auth/forgot-password
     *
     * Sends a password reset link to the email address.
     * Always returns 200 regardless of whether the email exists (anti-enumeration).
     * Body: { email }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {

        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    /**
     * POST /auth/reset-password
     *
     * Applies a new password using a valid reset token.
     * Body: { token, newPassword }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {

        return ResponseEntity.ok(authService.resetPassword(req));
    }

    // ── Protected endpoints (JWT required) ───────────────────────────────────

    /**
     * GET /auth/me
     *
     * Returns the authenticated user's profile.
     * The @AuthenticationPrincipal is populated by JwtAuthenticationFilter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(UserResponse.from(principal.getUser()));
    }

    /**
     * POST /auth/refresh
     *
     * Exchanges a valid refresh token for a new access token.
     * Body: { refreshToken }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest req) {

        return ResponseEntity.ok(authService.refresh(req));
    }

    /**
     * POST /auth/signout
     *
     * Invalidates the current access token by adding it to the blocklist.
     * The Authorization header is read directly to extract the token's JTI.
     */
    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> signout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(
                authService.signout(authHeader, principal.getUser().getId()));
    }

    /**
     * DELETE /auth/account
     *
     * Soft-deletes the authenticated user's account.
     * Also blocklists the current token so it can't be reused.
     */
    @DeleteMapping("/account")
    public ResponseEntity<MessageResponse> deleteAccount(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(
                authService.deleteAccount(principal.getUser().getId(), authHeader));
    }
}