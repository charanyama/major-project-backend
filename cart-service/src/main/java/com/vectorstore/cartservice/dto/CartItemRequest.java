package com.vectorstore.cartservice.dto;

import lombok.Data;

@Data
public class CartItemRequest {

    private String productId;
    private Integer quantity;
    private Double price;
}