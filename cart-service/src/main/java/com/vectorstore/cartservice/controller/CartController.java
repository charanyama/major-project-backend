package com.vectorstore.cartservice.controller;

import com.vectorstore.cartservice.dto.*;
import com.vectorstore.cartservice.entity.Cart;
import com.vectorstore.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Cart getCart(@RequestHeader("X-User-Id") String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public Cart addItem(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AddToCartRequest request) {
        return cartService.addItem(userId, request);
    }

    @PutMapping("/items/{itemId}")
    public Cart updateItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long itemId,
            @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public Cart removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long itemId) {
        return cartService.removeItem(userId, itemId);
    }

    @DeleteMapping
    public void clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
    }

    @PostMapping("/coupon")
    public Cart applyCoupon(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CouponRequest request) {
        return cartService.applyCoupon(userId, request.getCouponCode());
    }

    @DeleteMapping("/coupon")
    public Cart removeCoupon(@RequestHeader("X-User-Id") String userId) {
        return cartService.removeCoupon(userId);
    }

    @PostMapping("/merge")
    public Cart mergeGuestCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody MergeCartRequest request) {
        return cartService.mergeGuestCart(userId, request);
    }
}