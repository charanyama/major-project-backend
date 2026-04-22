package com.virtualstore.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * EmailDispatchService
 *
 * Renders a Thymeleaf HTML template, sends it via JavaMailSender,
 * and bubbles up any failures so callers can react before returning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDispatchService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromAddress;

    @Value("${notification.mail.from-name:VirtualStore}")
    private String fromName;

    public String send(String toEmail,
                       String subject,
                       String template,
                       Map<String, Object> payload) {

        Context ctx = new Context();
        if (payload != null) {
            payload.forEach(ctx::setVariable);
        }

        String html = templateEngine.process(template, ctx);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

            log.info("[Email] '{}' sent to {}", subject, toEmail);
            return null;  // SMTP doesn't expose a message id reliably

        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("[Email] Failed to send '{}' to {}: {}", subject, toEmail, ex.getMessage());
            throw new EmailDispatchException("SMTP send failed: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------ //
    //  Typed exception so the orchestrator can differentiate failures
    // ------------------------------------------------------------------ //

    public static class EmailDispatchException extends RuntimeException {
        public EmailDispatchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
