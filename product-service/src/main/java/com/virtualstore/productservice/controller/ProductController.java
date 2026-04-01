package com.virtualstore.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.virtualstore.productservice.dto.ProductDto;
import com.virtualstore.productservice.dto.ProductRequest;
import com.virtualstore.productservice.mapper.ProductMapper;
import com.virtualstore.productservice.model.Product;
import com.virtualstore.productservice.service.ProductService;

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
    public List<ProductDto> getAllProducts() {
        return ProductMapper.toDtoList(productService.findAll());
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable String id) {
        return ProductMapper.toDto(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody ProductRequest request) {
        Product saved = productService.createProduct(ProductMapper.toProduct(request));
        return ProductMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public ProductDto patchProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        Product patched = productService.patch(id, ProductMapper.toProduct(request));
        return ProductMapper.toDto(patched);
    }

    @PutMapping("/{id}")
    public ProductDto putProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, ProductMapper.toProduct(request));
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
            @RequestParam (required = false, defaultValue = "id") String sortBy,
            @RequestParam (required = false, defaultValue = "ASC") String sortDir,
            @RequestParam (required = false, defaultValue = "0") int page,
            @RequestParam (required = false, defaultValue = "10") int size) {
        return ProductMapper.toDtoList(productService.search(category, minPrice, maxPrice, sortBy, sortDir, page, size));
    }
}
