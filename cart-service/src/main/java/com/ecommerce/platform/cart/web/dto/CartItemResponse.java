package com.ecommerce.platform.cart.web.dto;

public record CartItemResponse(
        Long productId,
        String productName,
        int quantity
) {
}
