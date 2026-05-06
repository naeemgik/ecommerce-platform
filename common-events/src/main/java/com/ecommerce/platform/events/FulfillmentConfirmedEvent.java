package com.ecommerce.platform.events;

public record FulfillmentConfirmedEvent(
        Long orderId,
        Long customerId,
        String fulfillmentReference
) {
}
