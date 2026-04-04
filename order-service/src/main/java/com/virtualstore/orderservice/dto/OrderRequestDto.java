package com.virtualstore.orderservice.dto;

import lombok.Data;

@Data
public class OrderRequestDto {
    private String userId;
    private Double totalAmount;
}