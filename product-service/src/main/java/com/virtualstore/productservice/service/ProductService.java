package com.virtualstore.productservice.service;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.dto.ProductInteractionDto;
import com.virtualstore.productservice.entity.Product;
import com.virtualstore.productservice.exceptions.ProductNotFoundException;
import com.virtualstore.productservice.mapper.ProductMapper;
import com.virtualstore.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private KafkaProducerService producer;

    public static final String PRODUCT_INTERACTIONS = "product-interactions";
    public static final String PRODUCT_INDEXING = "product-indexing";

    public List<Product> findAll() {
        return repository.findAll();
    }

    // Internal use only
    private Product findById(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product findById(String id, String userId) {
        Product p = repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        ProductInteractionDto dto = ProductMapper.toProductInteractionDto(p, "VIEW_FULL_PRODUCT", userId);
        producer.publishProductInteraction(PRODUCT_INTERACTIONS, dto.getInteractionId(), dto);

        return p;
    }

    public Product createProduct(Product product) {
        product.setCreatedAtIfNull();

        Product saved = repository.save(product);
        ProductDto dto = ProductMapper.toDto(saved);

        producer.publishProduct(PRODUCT_INDEXING, dto.getId(), dto);

        return saved;
    }

    public Product patch(String id, Product updatedProduct, String userId) {
        Product existing = this.findById(id);
        Optional.ofNullable(updatedProduct.getName()).ifPresent(existing::setName);
        Optional.ofNullable(updatedProduct.getCategory()).ifPresent(existing::setCategory);
        Optional.ofNullable(updatedProduct.getSubcategory()).ifPresent(existing::setSubcategory);
        Optional.ofNullable(updatedProduct.getCategoryTree()).ifPresent(existing::setCategoryTree);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existing::setPrice);
        Optional.ofNullable(updatedProduct.getImgUrl()).ifPresent(existing::setImgUrl);
        Optional.ofNullable(updatedProduct.getRating()).ifPresent(existing::setRating);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existing::setDescription);
        Optional.ofNullable(updatedProduct.getOwner()).ifPresent(existing::setOwner);

        ProductInteractionDto dto = ProductMapper.toProductInteractionDto(existing, "PATCH_PRODUCT", userId);
        producer.publishProductInteraction(PRODUCT_INTERACTIONS, dto.getInteractionId(), dto);

        return repository.save(existing);
    }

    public Product update(String id, Product updatedProduct, String userId) {
        Product existing = this.findById(id); // throws 404 if not found

        existing.setName(updatedProduct.getName());
        existing.setCategory(updatedProduct.getCategory());
        existing.setSubcategory(updatedProduct.getSubcategory());
        existing.setCategoryTree(updatedProduct.getCategoryTree());
        existing.setPrice(updatedProduct.getPrice());
        existing.setImgUrl(updatedProduct.getImgUrl());
        existing.setRating(updatedProduct.getRating());
        existing.setDescription(updatedProduct.getDescription());
        existing.setOwner(updatedProduct.getOwner());

        ProductInteractionDto dto = ProductMapper.toProductInteractionDto(updatedProduct, "UPDATE_PRODUCT", userId);
        producer.publishProductInteraction(PRODUCT_INTERACTIONS, dto.getInteractionId(), dto);

        return repository.save(existing);
    }

    public List<Product> search(
            String category,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortDir,
            int page,
            int size) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pagable = PageRequest.of(page, size, sort);

        if (category != null && minPrice != null && maxPrice != null) {
            return repository.findByPriceBetweenAndCategory(category, minPrice, maxPrice, pagable).getContent();
        }

        if (category != null) {
            return repository.findByCategory(category, pagable).getContent();
        }

        if (minPrice != null && maxPrice != null) {
            return repository.findByPriceBetween(minPrice, maxPrice, pagable).getContent();
        }

        return repository.findAll(pagable).getContent();
    }

    public void updateProductRating(String productId, double avgRating, int totalRatings) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setAvgRating(avgRating);
        product.setTotalRatings(totalRatings);

        repository.save(product);
    }

    public void delete(String id) {
        Product existing = this.findById(id);

        repository.delete(existing);
    }
}
