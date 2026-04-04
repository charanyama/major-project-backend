package com.virtualstore.orderservice.dto;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInteractionDto {
    String interactionId;
    String productId;
    String userId;
    Instant timestamp;
    String interactionType;
    String serviceSource;
    Map<String, Object> metadata;
}
