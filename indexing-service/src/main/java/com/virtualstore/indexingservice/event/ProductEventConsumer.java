package com.virtualstore.indexingservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.virtualstore.indexingservice.service.IndexingService;

@Service
public class ProductEventConsumer {

    private final IndexingService indexingService;

    public ProductEventConsumer(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @KafkaListener(topics = "product-events", groupId = "indexing-group")
    public void consume(ProductEvent event) {

        switch (event.getEventType()) {

            case "CREATE":
            case "UPDATE":
                indexingService.indexProducts(
                        java.util.List.of(event.getProduct()));
                break;

            case "DELETE":
                indexingService.deleteProduct(event.getProduct().getId());
                break;
        }
    }
}