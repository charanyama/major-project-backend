package com.virtualstore.productservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualstore.productservice.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RatingEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductService productService;

    public RatingEventListener(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "analytics-events", groupId = "product-service")
    public void consume(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            String eventType = root.get("eventType").asText();

            if (!"ProductRatingUpdated".equals(eventType))
                return;

            JsonNode payload = root.get("payload");

            String productId = payload.get("productId").asText();
            double avgRating = payload.get("avgRating").asDouble();
            int totalRatings = payload.get("totalRatings").asInt();

            productService.updateProductRating(productId, avgRating, totalRatings);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}