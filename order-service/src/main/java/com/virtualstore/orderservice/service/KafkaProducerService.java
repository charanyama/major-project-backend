package com.virtualstore.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.virtualstore.orderservice.dto.OrderInteractionDto;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String TOPIC, String key, OrderInteractionDto dto) {
        kafkaTemplate.send(TOPIC, key, dto);
    }
}