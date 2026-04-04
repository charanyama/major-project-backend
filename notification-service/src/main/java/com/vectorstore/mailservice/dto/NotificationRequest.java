package com.vectorstore.mailservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String email;
    private String orderId;
}
