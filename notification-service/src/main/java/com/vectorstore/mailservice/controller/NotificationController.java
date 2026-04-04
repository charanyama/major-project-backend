package com.vectorstore.mailservice.controller;

import com.vectorstore.mailservice.dto.EmailRequest;
import com.vectorstore.mailservice.dto.NotificationRequest;
import com.vectorstore.mailservice.service.EmailService;
import com.vectorstore.mailservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @Autowired
    private EmailService emailService;
    @PostMapping("/order")
    public String sendNotification(@RequestBody NotificationRequest request) {
//        service.sendOrderNotification(request);
        return "Notification processed";
    }
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendOrderPlacedEmail(request.getEmail(), request.getName());
        return ResponseEntity.ok("Email Sent");
    }
}