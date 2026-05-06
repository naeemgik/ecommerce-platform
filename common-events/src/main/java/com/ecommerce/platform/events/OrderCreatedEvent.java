package com.ecommerce.platform.events;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        List<OrderLineItem> items,
        BigDecimal totalAmount
) {
}
