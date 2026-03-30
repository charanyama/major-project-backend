package com.vectorstore.orderservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private Double totalAmount;
    private String status;
}