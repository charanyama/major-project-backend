package com.vectorstore.cartservice.dto;

import lombok.Data;

@Data
public class CartItemRequest {

    private Long productId;
    private Integer quantity;
    private Double price;
}