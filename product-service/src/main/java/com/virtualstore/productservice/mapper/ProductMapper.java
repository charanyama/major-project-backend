package com.virtualstore.productservice.mapper;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.dto.ProductRequest;
import com.virtualstore.productservice.model.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice());
    }

    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    public static Product toProduct(ProductRequest request) {
        if (request == null) {
            return null;
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setSubcategory(request.getSubcategory());
        product.setCategoryTree(request.getCategoryTree());
        product.setImgUrl(request.getImgUrl());
        product.setOwner(request.getOwner());
        product.setBrand(request.getBrand());

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getRating() != null) {
            product.setRating(request.getRating());
        }

        return product;
    }

    public static Product updateProductFromRequest(Product existing, ProductRequest request) {
        if (existing == null || request == null) {
            return existing;
        }
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory());
        }
        if (request.getSubcategory() != null) {
            existing.setSubcategory(request.getSubcategory());
        }
        if (request.getCategoryTree() != null) {
            existing.setCategoryTree(request.getCategoryTree());
        }
        if (request.getImgUrl() != null) {
            existing.setImgUrl(request.getImgUrl());
        }
        if (request.getOwner() != null) {
            existing.setOwner(request.getOwner());
        }
        if (request.getBrand() != null) {
            existing.setBrand(request.getBrand());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getRating() != null) {
            existing.setRating(request.getRating());
        }
        return existing;
    }
}
