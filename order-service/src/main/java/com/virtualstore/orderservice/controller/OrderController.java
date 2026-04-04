package com.virtualstore.orderservice.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.virtualstore.orderservice.dto.OrderRequestDto;
import com.virtualstore.orderservice.dto.OrderResponseDto;
import com.virtualstore.orderservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping
    public OrderResponseDto createOrder(
            @RequestBody OrderRequestDto request,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymous";
        return service.createOrder(request, userId);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymous";
        return service.getOrderById(id);
    }

    @GetMapping
    public List<OrderResponseDto> getAllOrders(
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymous";
        return service.getAllOrders();
    }
}