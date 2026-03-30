package com.virutualstore.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.virutualstore.productservice.model.Product;
import com.virutualstore.productservice.service.ProductService;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PatchMapping("/{id}")
    public Product patchProduct(@PathVariable String id, @Valid @RequestBody Product product) {
        return productService.patch(id, product);
    }

    @PutMapping("/{id}")
    public Product putProduct(@PathVariable String id, @Valid @RequestBody Product product) {
        return productService.update(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.delete(id);
    }

    @GetMapping("/search")
    public List<Product> serachProducts(
            @RequestParam(required = false) @Valid String category,
            @RequestParam(required = false) @Valid Double minPrice,
            @RequestParam(required = false) @Valid Double maxPrice,
            @RequestParam (required = false, defaultValue = "id") String sortBy,
            @RequestParam (required = false, defaultValue = "ASC") String sortDir,
            @RequestParam (required = false, defaultValue = "0") int page,
            @RequestParam (required = false, defaultValue = "10") int size) {
        return productService.search(category, minPrice, maxPrice, sortBy, sortDir, page, size);
    }
}
