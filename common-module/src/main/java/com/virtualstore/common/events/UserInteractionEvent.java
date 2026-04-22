package com.virtualstore.common.events;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteractionEvent {
    private String eventId;
    private Instant timestamp;
    private String userId;
    private String productId;
    private String action;
    private String sourceService;
    private Map<String, String> metadata;

    public static UserInteractionEvent create(String userId, String productId, String action, String sourceService) {
        return UserInteractionEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(userId)
                .productId(productId)
                .action(action)
                .sourceService(sourceService)
                .metadata(Map.of())
                .build();
    }
}

