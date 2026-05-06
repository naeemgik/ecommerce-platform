package com.ecommerce.platform.product.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product %d was not found".formatted(productId));
    }
}
