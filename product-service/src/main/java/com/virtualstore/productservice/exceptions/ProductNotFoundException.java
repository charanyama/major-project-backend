package com.virtualstore.productservice.exceptions;

public class ProductNotFoundException extends RuntimeException {
    private final String prod_id;

    public ProductNotFoundException(String prod_id) {
        super(prod_id);
        this.prod_id = prod_id;
    }

    public String getProductId() {
        return this.prod_id;
    }
}