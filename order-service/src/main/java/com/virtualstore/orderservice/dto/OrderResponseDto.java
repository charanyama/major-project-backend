package com.virtualstore.orderservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponseDto {
    private Long id;
    private String userId;
    private Double totalAmount;
    private String status;
}