package com.virtualstore.user_service.listener;

import com.virtualstore.user_service.event.UserVerificationRequestedEvent;
import com.virtualstore.user_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserVerificationListener {

    private final EmailService emailService;

    @Async("emailTaskExecutor")
    @EventListener
    public void onUserVerificationRequested(UserVerificationRequestedEvent event) {
        log.debug("Dispatching verification email to {}", event.email());
        emailService.sendVerificationEmail(
                event.email(),
                event.fullName(),
                event.verificationToken());
    }
}
