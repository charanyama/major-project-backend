package com.virtualstore.productservice.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProductInteractionDto {
    String interactionId;
    String productId;
    String userId;
    String interactionType;
    Instant timestamp;
    
}
