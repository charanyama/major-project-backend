package com.virutualstore.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Address Entity for users
 * Represents shipping and billing addresses
 */
@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_address_type", columnList = "address_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Street address is required")
    @Column(nullable = false)
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State/Province is required")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Postal code is required")
    @Column(nullable = false)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    @Column(length = 500)
    private String addressDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Mark this address as default
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * Unset this address as default
     */
    public void unsetAsDefault() {
        this.isDefault = false;
    }
}
