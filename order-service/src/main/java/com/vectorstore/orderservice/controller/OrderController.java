package com.vectorstore.orderservice.controller;

import com.vectorstore.orderservice.dto.OrderRequest;
import com.vectorstore.orderservice.dto.OrderResponse;
import com.vectorstore.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return service.getOrderById(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return service.getAllOrders();
    }
}