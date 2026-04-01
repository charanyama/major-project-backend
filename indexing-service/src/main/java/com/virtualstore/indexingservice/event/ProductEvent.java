package com.virtualstore.indexingservice.event;

import com.virtualstore.indexingservice.dto.ProductDto;

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