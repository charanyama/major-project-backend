package com.virtualstore.productservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.virtualstore.productservice.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByCategory(String category, Pageable pagable);

    Page<Product> findBySubcategory(String subcategory, Pageable pagable);

    Page<Product> findByPriceBetween(Double min, Double max, Pageable pagable);

    Page<Product> findByPriceBetweenAndCategory(String category, Double min, Double max, Pageable pagable);

    @Aggregation(pipeline = {
            "{ '$group': { '_id': '$category' } }",
            "{ '$project': { 'category': '$_id', '_id': 0 } }"
    })
    List<String> findAllCategories();
}
