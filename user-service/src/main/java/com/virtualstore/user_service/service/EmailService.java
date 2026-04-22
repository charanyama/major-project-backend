package com.virtualstore.user_service.service;

import com.virtualstore.user_service.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * EmailService
 *
 * Sends HTML emails using Thymeleaf templates.
 * Templates live in src/main/resources/templates/:
 * verify-email.html — sent on registration / resend verification
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
     * Sends a password-reset link to the user.
     * Link format: {frontendUrl}/reset-password?token={token}
     */
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

    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String verificationUrl = appProperties.getFrontendUrl()
                + "/verify-email?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("verificationUrl", verificationUrl);
        ctx.setVariable("expiryHours", Math.max(1, appProperties.getEmailVerificationExpiryMs() / 3_600_000));

        sendHtmlEmail(
                toEmail,
                "Verify your email address",
                "verify-email",
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

        } catch (MailException | MessagingException | java.io.UnsupportedEncodingException e) {
            // Log and swallow — email failure should not crash the request
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
