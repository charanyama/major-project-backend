package com.vectorstore.cartservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String couponCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<CartItem> items = new ArrayList<>();

    // This calculates the grand total on the fly when the frontend requests the
    // cart.
    @Transient
    public Double getGrandTotal() {
        if (items == null || items.isEmpty())
            return 0.0;

        double total = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Basic example: Apply a 10% discount if any coupon code is present
        if (couponCode != null && !couponCode.isBlank()) {
            total = total * 0.90;
        }

        return total;
    }
}