package com.virutualstore.productservice.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private String category;
    private String subcategory;
    private String categoryTree;
    private String price;
    private String brand;
    private String rating;
}
