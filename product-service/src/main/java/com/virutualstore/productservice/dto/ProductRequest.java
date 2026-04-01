package com.virutualstore.productservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private String category;
    private String subcategory;
    private String categoryTree;
    private BigDecimal price;
    private String imgUrl;
    private String owner;
    private String brand;
    private Double rating;
}
