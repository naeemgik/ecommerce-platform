package com.ecommerce.platform.events;

import java.math.BigDecimal;

public record OrderLineItem(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
