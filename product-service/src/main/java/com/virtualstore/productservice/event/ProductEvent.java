package com.virtualstore.productservice.event;

import com.virtualstore.productservice.dto.ProductDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {

    private String eventType; // CREATE, UPDATE, DELETE
    private ProductDto product;
}