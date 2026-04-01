package com.virtualstore.productservice.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductDto {
    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;

    public ProductDto(String id, String name, String description, String category, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }
}