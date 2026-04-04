package com.vectorstore.mailservice.service;

import com.vectorstore.mailservice.dto.NotificationRequest;
import com.vectorstore.mailservice.entity.Notification;
import com.vectorstore.mailservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {


    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private NotificationRepository repository;


//    public void sendOrderNotification(NotificationRequest request) {
//
//        String messageText = "Your order with ID " + request.getOrderId()
//                + " has been successfully placed. It will be delivered in 2-3 days.";
//
//        String status = "SENT";
//
//        try {
//            System.out.println("Attempting to send email to: {}" + request.getEmail());
//
//            SimpleMailMessage mail = new SimpleMailMessage();
//            mail.setTo(request.getEmail());
//            mail.setSubject("Order Confirmation");
//            mail.setText(messageText);
//
//            mailSender.send(mail);
//
//        } catch (Exception e) {
//            status = "FAILED";
//        }
//
//        // Save in DB (audit/report purpose)
//        Notification notification = Notification.builder()
//                .email(request.getEmail())
//                .message(messageText)
//                .status(status)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        repository.save(notification);
//    }

}

