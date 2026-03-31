package com.vectorstore.cartservice.service;

import com.vectorstore.cartservice.dto.CartItemRequest;
import com.vectorstore.cartservice.entity.Cart;
import com.vectorstore.cartservice.entity.CartItem;
import com.vectorstore.cartservice.exception.CartNotFoundException;
import com.vectorstore.cartservice.exception.ProductNotFoundException;
import com.vectorstore.cartservice.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // ✅ Get Cart (Throws Exception if not found)
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    // ✅ Create Cart (Internal Use)
    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    // ✅ Add Item to Cart
    public Cart addItem(Long userId, CartItemRequest request) {

        // Validate request
        if (request.getProductId() == null) {
            throw new ProductNotFoundException(null);
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        // Check if item already exists → update quantity
        boolean itemExists = false;

        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                itemExists = true;
                break;
            }
        }

        // If new item → add
        if (!itemExists) {
            CartItem item = new CartItem();
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setPrice(request.getPrice());
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    // ✅ Remove Item from Cart
    public Cart removeItem(Long userId, Long productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        boolean removed = cart.getItems()
                .removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new ProductNotFoundException(productId);
        }

        return cartRepository.save(cart);
    }

    // ✅ Clear Cart
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}