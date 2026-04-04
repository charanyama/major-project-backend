package com.vectorstore.mailservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;



@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendOrderPlacedEmail(String to, String name) {
        try {
            // 🔹 Prepare data for template
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", to);
            context.setVariable("link", "http://localhost:3000/login");

            // 🔹 Process template
            String htmlContent = templateEngine.process("welcome-email", context);

            // 🔹 Create email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Welcome to Our Platform 🚀");
            helper.setText(htmlContent, true);
            helper.setFrom("your-email@gmail.com");

            mailSender.send(message);

            System.out.println("✅ HTML Email sent!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}