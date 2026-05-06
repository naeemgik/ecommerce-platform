package com.ecommerce.platform.events;

public record PaymentFailedEvent(
        Long orderId,
        Long customerId,
        String reason
) {
}
