package com.virutualstore.productservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private static final String TOPIC = "product-events";

    public ProductEventProducer(KafkaTemplate<String, ProductEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ProductEvent event) {
        kafkaTemplate.send(TOPIC, event.getProduct().getId(), event);
    }
}