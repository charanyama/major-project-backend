package com.virtualstore.productservice.service;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.event.ProductEvent;
import com.virtualstore.productservice.event.ProductEventProducer;
import com.virtualstore.productservice.exceptions.ProductNotFoundException;
import com.virtualstore.productservice.mapper.ProductMapper;
import com.virtualstore.productservice.model.Product;
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
    private ProductEventProducer producer;

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(Product product) {
        product.setCreatedAtIfNull();

        Product saved = repository.save(product);
        ProductDto dto = ProductMapper.toDto(saved);
        producer.publish(new ProductEvent("CREATE", dto));

        return saved;
    }

    public Product patch(String id, Product updatedProduct) {
        Product existing = findById(id);

        Optional.ofNullable(updatedProduct.getName()).ifPresent(existing::setName);
        Optional.ofNullable(updatedProduct.getCategory()).ifPresent(existing::setCategory);
        Optional.ofNullable(updatedProduct.getSubcategory()).ifPresent(existing::setSubcategory);
        Optional.ofNullable(updatedProduct.getCategoryTree()).ifPresent(existing::setCategoryTree);
        Optional.ofNullable(updatedProduct.getPrice()).ifPresent(existing::setPrice);
        Optional.ofNullable(updatedProduct.getImgUrl()).ifPresent(existing::setImgUrl);
        Optional.ofNullable(updatedProduct.getRating()).ifPresent(existing::setRating);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existing::setDescription);
        Optional.ofNullable(updatedProduct.getOwner()).ifPresent(existing::setOwner);

        producer.publish(new ProductEvent("UPDATE", ProductMapper.toDto(existing)));

        return repository.save(existing);
    }

    public Product update(String id, Product updatedProduct) {
        Product existing = findById(id); // throws 404 if not found

        existing.setName(updatedProduct.getName());
        existing.setCategory(updatedProduct.getCategory());
        existing.setSubcategory(updatedProduct.getSubcategory());
        existing.setCategoryTree(updatedProduct.getCategoryTree());
        existing.setPrice(updatedProduct.getPrice());
        existing.setImgUrl(updatedProduct.getImgUrl());
        existing.setRating(updatedProduct.getRating());
        existing.setDescription(updatedProduct.getDescription());
        existing.setOwner(updatedProduct.getOwner());

        producer.publish(new ProductEvent("UPDATE", ProductMapper.toDto(existing)));

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

    public void delete(String id) {
        Product existing = findById(id);

        producer.publish(new ProductEvent("DELETE", ProductMapper.toDto(existing)));

        repository.delete(existing);
    }
}
