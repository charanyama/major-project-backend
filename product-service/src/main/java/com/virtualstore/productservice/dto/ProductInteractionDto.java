package com.virtualstore.productservice.dto;

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
public class ProductInteractionDto {
    String interactionId;
    String productId;
    String userId;
    Instant timestamp;
    String interactionType;
    String serviceSource;
    Map<String, Object> metadata; 
}
