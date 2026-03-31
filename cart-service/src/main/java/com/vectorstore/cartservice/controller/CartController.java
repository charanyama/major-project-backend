package com.vectorstore.cartservice.controller;

import com.vectorstore.cartservice.dto.CartItemRequest;
import com.vectorstore.cartservice.entity.Cart;
import com.vectorstore.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Get Cart
    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    // Add Item
    @PostMapping("/{userId}/add")
    public Cart addItem(@PathVariable Long userId,
                        @RequestBody CartItemRequest request) {
        return cartService.addItem(userId, request);
    }

    // Remove Item
    @DeleteMapping("/{userId}/remove/{productId}")
    public Cart removeItem(@PathVariable Long userId,
                           @PathVariable Long productId) {
        return cartService.removeItem(userId, productId);
    }

    // Clear Cart
    @DeleteMapping("/{userId}/clear")
    public String clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return "Cart cleared";
    }
}