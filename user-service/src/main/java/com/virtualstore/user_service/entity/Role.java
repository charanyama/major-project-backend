package com.virtualstore.user_service.entity;

/**
 * Enum representing different user roles in the system
 */
public enum Role {
    CUSTOMER,
    SELLER,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
