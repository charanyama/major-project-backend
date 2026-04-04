package com.virtualstore.orderservice.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.virtualstore.orderservice.dto.OrderInteractionDto;
import com.virtualstore.orderservice.dto.OrderResponseDto;
import com.virtualstore.orderservice.entity.Order;

public class OrderMapper {
    public static OrderInteractionDto toInteractionDto(Order o, String interactionType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", o != null ? o.getId() : null);
        metadata.put("productSKU", o != null ? o.getProductSkus() : null);
        metadata.put("totalAmount", o != null ? o.getTotalAmount() : null);

        return OrderInteractionDto
                .builder()
                .interactionId("IA-" + UUID.randomUUID().toString().substring(0, 15))
                .interactionType(interactionType)
                .metadata(metadata)
                .build();
    }

    public static OrderResponseDto toDto(Order o) {
        return OrderResponseDto
                .builder()
                .id(o.getId())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .build();
    }
}
