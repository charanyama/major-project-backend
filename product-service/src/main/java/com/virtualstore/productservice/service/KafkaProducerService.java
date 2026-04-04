package com.virtualstore.productservice.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.dto.ProductInteractionDto;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, Object> template;

    public CompletableFuture<Void> publishProduct(String TOPIC, String id, ProductDto product) {
        this.template.send(TOPIC, id, product);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> publishProductInteraction(String TOPIC, String id, ProductInteractionDto dto) {
        this.template.send(TOPIC, id, dto);
        return CompletableFuture.completedFuture(null);
    }
}