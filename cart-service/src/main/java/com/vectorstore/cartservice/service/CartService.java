package com.vectorstore.cartservice.service;

import com.vectorstore.cartservice.dto.AddToCartRequest;
import com.vectorstore.cartservice.dto.MergeCartRequest;
import com.vectorstore.cartservice.dto.UpdateCartItemRequest;
import com.vectorstore.cartservice.entity.Cart;
import com.vectorstore.cartservice.entity.CartItem;
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

    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            return cartRepository.save(cart);
        });
    }

    public Cart addItem(String userId, AddToCartRequest request) {
        if (request.getProductId() == null)
            throw new ProductNotFoundException(null);
        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");
        if (request.getPrice() == null || request.getPrice() < 0)
            throw new IllegalArgumentException("Price cannot be negative");

        Cart cart = getCart(userId);
        boolean itemExists = false;

        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(request.getProductId())) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            CartItem item = new CartItem();
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setPrice(request.getPrice());
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    public Cart updateItem(String userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getCart(userId);

        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(itemToUpdate);
        } else {
            itemToUpdate.setQuantity(request.getQuantity());
        }

        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, Long itemId) {
        Cart cart = getCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));

        if (!removed) {
            throw new IllegalArgumentException("Item not found in cart");
        }

        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        cart.setCouponCode(null);
        cartRepository.save(cart);
    }

    public Cart applyCoupon(String userId, String couponCode) {
        Cart cart = getCart(userId);
        cart.setCouponCode(couponCode);
        return cartRepository.save(cart);
    }

    public Cart removeCoupon(String userId) {
        Cart cart = getCart(userId);
        cart.setCouponCode(null);
        return cartRepository.save(cart);
    }

    public Cart mergeGuestCart(String userId, MergeCartRequest request) {
        Cart cart = getCart(userId);

        for (MergeCartRequest.MergeItem guestItem : request.getItems()) {
            CartItem existingItem = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + guestItem.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setProductId(guestItem.getProductId());
                newItem.setQuantity(guestItem.getQuantity());
                // In a real app, you might want to fetch the real price from a Product Service
                // here
                newItem.setPrice(0.0);
                cart.getItems().add(newItem);
            }
        }
        return cartRepository.save(cart);
    }
}