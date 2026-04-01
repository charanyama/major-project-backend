package com.virtualstore.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.virtualstore.productservice.model.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByCategory(String category, Pageable pagable);

    Page<Product> findBySubcategory(String subcategory, Pageable pagable);

    Page<Product> findByPriceBetween(Double min, Double max, Pageable pagable);

    Page<Product> findByPriceBetweenAndCategory(String category, Double min, Double max, Pageable pagable);
}
