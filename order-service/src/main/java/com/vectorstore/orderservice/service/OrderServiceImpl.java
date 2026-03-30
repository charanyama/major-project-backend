package com.vectorstore.orderservice.service;

import com.vectorstore.orderservice.dto.OrderRequest;
import com.vectorstore.orderservice.dto.OrderResponse;
import com.vectorstore.orderservice.entity.Order;
import com.vectorstore.orderservice.exception.ResourceNotFoundException;
import com.vectorstore.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(request.getTotalAmount())
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        Order saved = repository.save(order);

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }
}