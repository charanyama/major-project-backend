package com.virtualstore.common.events;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {
    private String eventId;
    private Instant timestamp;
    private String eventType;
    private String sourceService;
    private String userId;
    private String productId;
    private Map<String, String> metadata;

    public static ProductEvent createProduct(String productId, String userId, String sourceService) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .eventType("PRODUCT_CREATED")
                .sourceService(sourceService)
                .userId(userId)
                .productId(productId)
                .metadata(Map.of())
                .build();
    }
}
