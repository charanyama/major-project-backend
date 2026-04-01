package com.virtualstore.indexingservice.controller;

import com.virtualstore.indexingservice.dto.ProductDto;
import com.virtualstore.indexingservice.service.IndexingService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/index")
public class IndexController {

    private final IndexingService indexingService;

    public IndexController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    // Push model
    @PostMapping("/products")
    public String indexProducts(@RequestBody List<ProductDto> products) {
        indexingService.indexProducts(products);
        return "Products indexed successfully";
    }
}