package com.virtualstore.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.dto.ProductRequest;
import com.virtualstore.productservice.entity.Product;
import com.virtualstore.productservice.mapper.ProductMapper;
import com.virtualstore.productservice.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        return ProductMapper.toDtoList(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable String id,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymus";
        // log.info("Request received from User: {} with Id: {}", userId, id);
        System.out.println("\n\n\nRequest of user for product with id: " + id);
        System.out.println(productService.findById(id, userId));
        return ResponseEntity.ok(productService.findById(id, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody ProductRequest request) {
        Product saved = productService.createProduct(ProductMapper.toProduct(request));
        return ProductMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public ProductDto patchProduct(@PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymus";

        Product patched = productService.patch(id, ProductMapper.toProduct(request), userId);
        return ProductMapper.toDto(patched);
    }

    @PutMapping("/{id}")
    public ProductDto putProduct(@PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymus";

        Product updated = productService.update(id, ProductMapper.toProduct(request), userId);
        return ProductMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.delete(id);
    }

    @GetMapping("/search")
    public Page<ProductDto> serachProducts(
            @RequestParam(required = false) @Valid String category,
            @RequestParam(required = false) @Valid Double minPrice,
            @RequestParam(required = false) @Valid Double maxPrice,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Page<Product> res = productService.search(category, minPrice, maxPrice, sortBy, sortDir, page, size);
        return res.map(ProductMapper::toDto);
    }

    @GetMapping("/categories")
    public List<String> getProductsByCategory() {
    
        return productService.getAllCategories();
    }

    @GetMapping("/wishlist")
    public List<String> getWishList() {
        return List.of();
    }
}
