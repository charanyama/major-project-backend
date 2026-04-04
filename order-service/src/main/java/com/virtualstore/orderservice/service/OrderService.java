package com.virtualstore.orderservice.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.virtualstore.orderservice.dto.OrderInteractionDto;
import com.virtualstore.orderservice.dto.OrderRequestDto;
import com.virtualstore.orderservice.dto.OrderResponseDto;
import com.virtualstore.orderservice.entity.Order;
import com.virtualstore.orderservice.mapper.OrderMapper;
import com.virtualstore.orderservice.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private KafkaProducerService publisher;

    public OrderResponseDto createOrder(OrderRequestDto request, String userId) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        Order saved = repository.save(order);
        OrderInteractionDto dto = OrderMapper.toInteractionDto(order, "CREATE_ORDER");

        publisher.publish("interactions", userId, dto);

        return OrderMapper.toDto(saved);
    }

    public OrderResponseDto getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return OrderMapper.toDto(order);
    }

    public List<OrderResponseDto> getAllOrders() {
        return repository.findAll()
                .stream()
                .map(OrderMapper::toDto)
                .toList();
    }
}