package com.virtualstore.user_service.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.virtualstore.user_service.config.AppProperties;
import com.virtualstore.user_service.config.JwtProperties;
import com.virtualstore.user_service.dto.request.*;
import com.virtualstore.user_service.dto.response.AuthResponse;
import com.virtualstore.user_service.dto.response.MessageResponse;
import com.virtualstore.user_service.entity.InvalidatedToken;
import com.virtualstore.user_service.entity.Role;
import com.virtualstore.user_service.entity.User;
import com.virtualstore.user_service.exception.*;
import com.virtualstore.user_service.repository.InvalidatedTokenRepository;
import com.virtualstore.user_service.repository.UserRepository;
import com.virtualstore.user_service.security.UserPrincipal;

import java.time.Instant;
import java.util.UUID;

/**
 * AuthService
 *
 * All authentication and account lifecycle business logic.
 *
 * Methods:
 * signup() — create account, send verification email
 * verifyEmail() — activate account via token
 * resendVerification() — re-send verification email
 * login() — authenticate, issue access + refresh tokens
 * refresh() — exchange refresh token for new access token
 * signout() — blocklist the current access token
 * forgotPassword() — send password reset email
 * resetPassword() — apply new password via reset token
 * deleteAccount() — soft-delete the authenticated user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final InvalidatedTokenRepository invalidatedTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final EmailService emailService;
        private final AppProperties appProperties;
        private final JwtProperties jwtProperties;

        // ─────────────────────────────────────────────────────────────────────────
        // Signup
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Registers a new user.
         *
         * Steps:
         * 1. Check email uniqueness
         * 2. Block ADMIN self-registration (ADMIN accounts must be created internally)
         * 3. Hash password with BCrypt
         * 4. Persist user with enabled=false
         * 5. Send verification email asynchronously
         */
        public MessageResponse signup(SignupRequest req) {
                if (userRepository.existsByEmail(req.getEmail())) {
                        throw new EmailAlreadyExistsException(req.getEmail());
                }

                // Policy: ADMIN accounts cannot be self-registered
                if (req.getRole() == Role.ADMIN) {
                        throw new AppException(
                                        "ADMIN accounts cannot be self-registered.",
                                        org.springframework.http.HttpStatus.FORBIDDEN);
                }

                String verificationToken = UUID.randomUUID().toString();
                Instant verificationExpiry = Instant.now()
                                .plusMillis(appProperties.getVerificationTokenExpiryMs());

                User user = User.builder()
                                .email(req.getEmail())
                                .fullName(req.getFullName())
                                .passwordHash(passwordEncoder.encode(req.getPassword()))
                                .role(req.getRole())
                                .enabled(false)
                                .verificationToken(verificationToken)
                                .verificationTokenExpiry(verificationExpiry)
                                .build();

                userRepository.save(user);

                // Fire-and-forget on the email thread pool
                emailService.sendVerificationEmail(
                                user.getEmail(), user.getFullName(), verificationToken);

                log.info("User registered: {} as {}", user.getEmail(), user.getRole());
                return new MessageResponse(
                                "Registration successful. Please check your email to verify your account.");
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Email verification
        // ─────────────────────────────────────────────────────────────────────────

        public MessageResponse verifyEmail(String token) {
                User user = userRepository.findByVerificationToken(token)
                                .orElseThrow(() -> new InvalidTokenException(
                                                "Verification link is invalid or has already been used."));

                if (Instant.now().isAfter(user.getVerificationTokenExpiry())) {
                        throw new InvalidTokenException(
                                        "Verification link has expired. Please request a new one.");
                }

                user.setEnabled(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                userRepository.save(user);

                log.info("Email verified for user: {}", user.getEmail());
                return new MessageResponse("Email verified successfully. You can now log in.");
        }

        public MessageResponse resendVerification(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));

                if (user.isEnabled()) {
                        return new MessageResponse("Your account is already verified.");
                }

                String newToken = UUID.randomUUID().toString();
                user.setVerificationToken(newToken);
                user.setVerificationTokenExpiry(
                                Instant.now().plusMillis(appProperties.getVerificationTokenExpiryMs()));
                userRepository.save(user);

                emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), newToken);
                return new MessageResponse("Verification email resent. Please check your inbox.");
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Login
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Authenticates credentials and issues access + refresh tokens.
         *
         * Spring's AuthenticationManager handles:
         * - Password verification (BCrypt)
         * - DisabledException if enabled=false (not yet verified)
         * - LockedException if accountNonLocked=false
         * - BadCredentialsException on wrong password
         */
        public AuthResponse login(LoginRequest req) {
                // Throws BadCredentialsException or DisabledException on failure —
                // both are caught by GlobalExceptionHandler
                Authentication auth = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

                User user = ((UserPrincipal) auth.getPrincipal()).getUser();

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                log.info("User logged in: {}", user.getEmail());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs())
                                .id(user.getId())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .role(user.getRole())
                                .build();
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Token refresh
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Exchanges a valid refresh token for a new access token.
         * Does NOT rotate the refresh token (add rotation here if needed).
         */
        public AuthResponse refresh(RefreshTokenRequest req) {
                Claims claims = jwtService.parseToken(req.getRefreshToken())
                                .orElseThrow(() -> new InvalidTokenException(
                                                "Refresh token is invalid or expired. Please log in again."));

                // Ensure this is actually a refresh token
                if (!"refresh".equals(claims.get("type", String.class))) {
                        throw new InvalidTokenException("Invalid token type.");
                }

                String userId = claims.getSubject();
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(userId));

                if (!user.isEnabled() || user.isDeleted()) {
                        throw new AppException("Account is inactive.",
                                        org.springframework.http.HttpStatus.FORBIDDEN);
                }

                String newAccessToken = jwtService.generateAccessToken(user);

                return AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(req.getRefreshToken()) // same refresh token
                                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiryMs())
                                .id(user.getId())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .role(user.getRole())
                                .build();
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Signout
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Invalidates the current access token by adding its JTI to the blocklist.
         *
         * The token is extracted from the Authorization header (already validated
         * by JwtAuthenticationFilter before this method is called).
         * MongoDB TTL index auto-removes the blocklist entry once the token
         * would have expired naturally.
         */
        public MessageResponse signout(String authorizationHeader, String userId) {
                String token = authorizationHeader.substring(7); // strip "Bearer "

                String jti = jwtService.extractJtiUnsafe(token)
                                .orElseThrow(() -> new InvalidTokenException("Cannot extract token identifier."));

                Instant expiry = jwtService.extractExpiryUnsafe(token)
                                .orElse(Instant.now().plusSeconds(3600)); // fallback: 1 hour

                // Idempotent — if JTI already blocklisted, no-op
                if (!invalidatedTokenRepository.existsByJti(jti)) {
                        invalidatedTokenRepository.save(
                                        InvalidatedToken.builder()
                                                        .jti(jti)
                                                        .userId(userId)
                                                        .expiresAt(expiry)
                                                        .invalidatedAt(Instant.now())
                                                        .build());
                }

                log.info("User {} signed out, token jti={} blocklisted", userId, jti);
                return new MessageResponse("Signed out successfully.");
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Password reset
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Initiates password reset flow.
         * Always returns the same message regardless of whether the email exists —
         * prevents user enumeration attacks.
         */
        public MessageResponse forgotPassword(ForgotPasswordRequest req) {
                userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
                        String resetToken = UUID.randomUUID().toString();
                        user.setPasswordResetToken(resetToken);
                        user.setPasswordResetTokenExpiry(
                                        Instant.now().plusMillis(appProperties.getPasswordResetExpiryMs()));
                        userRepository.save(user);
                        emailService.sendPasswordResetEmail(
                                        user.getEmail(), user.getFullName(), resetToken);
                });
                return new MessageResponse(
                                "If that email is registered, a password reset link has been sent.");
        }

        public MessageResponse resetPassword(ResetPasswordRequest req) {
                User user = userRepository.findByPasswordResetToken(req.getToken())
                                .orElseThrow(() -> new InvalidTokenException(
                                                "Password reset link is invalid or has already been used."));

                if (Instant.now().isAfter(user.getPasswordResetTokenExpiry())) {
                        throw new InvalidTokenException("Password reset link has expired.");
                }

                user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiry(null);
                userRepository.save(user);

                log.info("Password reset for user: {}", user.getEmail());
                return new MessageResponse("Password reset successfully. You can now log in.");
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Account deletion
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Soft-deletes the authenticated user's account.
         * Sets deleted=true and enabled=false — data is retained for audit purposes.
         * Hard deletion can be implemented as a scheduled cleanup job.
         */
        public MessageResponse deleteAccount(String userId, String authorizationHeader) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(userId));

                user.setDeleted(true);
                user.setEnabled(false);
                userRepository.save(user);

                // Also blocklist the current token so it can't be used after deletion
                signout(authorizationHeader, userId);

                log.info("Account soft-deleted for userId: {}", userId);
                return new MessageResponse("Account deleted successfully.");
        }
}