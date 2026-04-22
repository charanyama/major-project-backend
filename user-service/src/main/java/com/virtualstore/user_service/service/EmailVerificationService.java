package com.virtualstore.user_service.service;

import com.virtualstore.user_service.config.AppProperties;
import com.virtualstore.user_service.entity.Status;
import com.virtualstore.user_service.entity.User;
import com.virtualstore.user_service.event.UserVerificationRequestedEvent;
import com.virtualstore.user_service.exception.InvalidTokenException;
import com.virtualstore.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties appProperties;

    public void prepareForVerification(User user) {
        user.setEmailVerified(false);
        user.setEnabled(false);
        user.setStatus(Status.PENDING);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(
                Instant.now().plusMillis(appProperties.getEmailVerificationExpiryMs()));
    }

    public void dispatchVerificationEmail(User user) {
        eventPublisher.publishEvent(new UserVerificationRequestedEvent(
                user.getEmail(),
                user.getFullName(),
                user.getVerificationToken()));
    }

    public void dispatchVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElse(null);

        if (user == null) {
            return;
        }

        if (user.isEmailVerified()) {
            return;
        }

        prepareForVerification(user);
        userRepository.save(user);
        dispatchVerificationEmail(user);
        log.info("Verification email resent to {}", user.getEmail());
    }

    public void verify(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException(
                        "Verification link is invalid or has already been used."));

        if (user.getVerificationTokenExpiry() == null ||
                Instant.now().isAfter(user.getVerificationTokenExpiry())) {
            throw new InvalidTokenException("Verification link has expired.");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setStatus(Status.ACTIVE);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        log.info("Email verified for user {}", user.getEmail());
    }
}
