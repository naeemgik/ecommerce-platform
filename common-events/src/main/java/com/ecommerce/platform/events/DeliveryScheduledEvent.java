package com.ecommerce.platform.events;

public record DeliveryScheduledEvent(
        Long orderId,
        Long customerId,
        String deliveryReference
) {
}
