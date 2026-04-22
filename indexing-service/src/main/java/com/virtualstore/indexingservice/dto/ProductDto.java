package com.virtualstore.indexingservice.dto;

import lombok.Data;

@Data
public class ProductDto {
    private String id;
    private String title;
    private String category;
    private String imgUrl;
    private Double price;
    private Double rating;
    private String brand;
}