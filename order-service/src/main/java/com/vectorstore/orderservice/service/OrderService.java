package com.vectorstore.orderservice.service;

import com.vectorstore.orderservice.dto.OrderRequest;
import com.vectorstore.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrders();
}