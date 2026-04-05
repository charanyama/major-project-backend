package com.virtualstore.user_service.service;

import com.virtualstore.user_service.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * EmailService
 *
 * Sends HTML emails using Thymeleaf templates.
 * All methods are @Async — email I/O never blocks the HTTP thread.
 *
 * Templates live in src/main/resources/templates/:
 * verify-email.html — sent on signup
 * reset-password.html — sent on forgot-password
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private AppProperties appProperties;
    
    /**
     * Sends an email verification link to the user after signup.
     * Link format: {frontendUrl}/verify-email?token={token}
     */
    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String verifyUrl = appProperties.getFrontendUrl()
                + "/verify-email?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("verifyUrl", verifyUrl);
        ctx.setVariable("expiryHours", 24);

        sendHtmlEmail(
                toEmail,
                "Verify your email address",
                "verify-email",
                ctx);
    }

    /**
     * Sends a password-reset link to the user.
     * Link format: {frontendUrl}/reset-password?token={token}
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        String resetUrl = appProperties.getFrontendUrl()
                + "/reset-password?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("resetUrl", resetUrl);
        ctx.setVariable("expiryHours", 1);

        sendHtmlEmail(
                toEmail,
                "Reset your password",
                "reset-password",
                ctx);
    }

    private void sendHtmlEmail(String to, String subject,
            String template, Context ctx) {
        try {
            String html = templateEngine.process(template, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(
                    appProperties.getMail().getFrom(),
                    appProperties.getMail().getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = isHtml

            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Log and swallow — email failure should not crash the request
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}