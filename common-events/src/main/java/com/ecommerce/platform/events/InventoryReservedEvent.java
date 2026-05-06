package com.ecommerce.platform.events;

import java.math.BigDecimal;
import java.util.List;

public record InventoryReservedEvent(
        Long orderId,
        Long customerId,
        List<OrderLineItem> items,
        BigDecimal totalAmount
) {
}
