package com.virtualstore.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        return ProductMapper.toDtoList(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable String id,
            @RequestHeader(value = "X-UserId", required = false) String userId) {
        if (userId == null)
            userId = "anonymus";
        // log.info("Request received from User: {}", userId);
        return ResponseEntity.ok(ProductMapper.toDto(productService.findById(id, userId)));
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
    public List<ProductDto> serachProducts(
            @RequestParam(required = false) @Valid String category,
            @RequestParam(required = false) @Valid Double minPrice,
            @RequestParam(required = false) @Valid Double maxPrice,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ProductMapper
                .toDtoList(productService.search(category, minPrice, maxPrice, sortBy, sortDir, page, size));
    }
}
