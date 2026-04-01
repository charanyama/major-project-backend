package com.vectorstore.inventory_service.service;

import com.vectorstore.inventory_service.dto.InventoryResponse;
import com.vectorstore.inventory_service.exceptionhandling.ResourceNotFoundException;
import com.vectorstore.inventory_service.entity.Inventory;
import com.vectorstore.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryResponse isInStock(String skuCode) {
        Inventory inventory = repository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return new InventoryResponse(
                skuCode,
                inventory.getQuantity() > 0
        );
    }

    public Inventory addInventory(Inventory inventory) {
        return repository.save(inventory);
    }

    public Inventory updateStock(String skuCode, Integer quantity) {
        Inventory inventory = repository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        inventory.setQuantity(quantity);
        return repository.save(inventory);
    }
}