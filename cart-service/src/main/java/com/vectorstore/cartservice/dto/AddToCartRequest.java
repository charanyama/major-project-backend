package com.vectorstore.cartservice.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String productId;
    private Integer quantity;
    private Double price;
}