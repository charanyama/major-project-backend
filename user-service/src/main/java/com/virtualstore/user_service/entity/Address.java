package com.virtualstore.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

/**
 * Address Entity for users
 * Represents shipping and billing addresses
 */
@Document(collection = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @Builder.Default
    private String addressId = UUID.randomUUID().toString().substring(0, 7);

    @Indexed
    @Field("user_id")
    private String userId;

    @NotBlank(message = "Street address is required")
    @Field("street_address")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Field("postal_code")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    @Field("address_details")
    private String addressDetails;

    @Indexed
    @Field("address_type")
    private String addressType;

    @Builder.Default
    @Field("is_default")
    private Boolean isDefault = false;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Instant updatedAt;

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }
}
