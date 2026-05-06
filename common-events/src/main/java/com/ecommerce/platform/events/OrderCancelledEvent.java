package com.ecommerce.platform.events;

public record OrderCancelledEvent(
        Long orderId,
        Long customerId,
        String reason
) {
}
