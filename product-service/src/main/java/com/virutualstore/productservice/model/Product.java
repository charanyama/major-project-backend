package com.virutualstore.productservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "seed_products")
@Data
@Builder
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String subcategory;

    private String categoryTree;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "Image URL is required")
    private String imgUrl;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be >= 0")
    @DecimalMax(value = "5.0", message = "Rating must be <= 5")
    private Double rating;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Owner is required")
    private String owner;

    @NotBlank(message = "Brand must be specified")
    private String brand;

    private Instant createdAt;

    public Product() {
    }

    public void setCreatedAtIfNull() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
