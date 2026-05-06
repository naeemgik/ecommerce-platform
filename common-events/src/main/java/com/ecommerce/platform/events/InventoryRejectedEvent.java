package com.ecommerce.platform.events;

public record InventoryRejectedEvent(
        Long orderId,
        Long customerId,
        String reason
) {
}
