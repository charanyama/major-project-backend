package com.vectorstore.inventory_service.controller;

import com.vectorstore.inventory_service.dto.InventoryResponse;
import com.vectorstore.inventory_service.entity.Inventory;
import com.vectorstore.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @GetMapping("/{skuCode}")
    public InventoryResponse isInStock(@PathVariable String skuCode) {
        return service.isInStock(skuCode);
    }

    @PostMapping
    public Inventory addInventory(@RequestBody Inventory inventory) {
        return service.addInventory(inventory);
    }

    @PutMapping("/{skuCode}")
    public Inventory updateStock(
            @PathVariable String skuCode,
            @RequestParam Integer quantity) {
        return service.updateStock(skuCode, quantity);
    }
}