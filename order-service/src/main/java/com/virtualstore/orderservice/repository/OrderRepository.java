package com.virtualstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualstore.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}