package com.ecommerce.platform.events;

import java.math.BigDecimal;

public record ProductView(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean available
) {
}
