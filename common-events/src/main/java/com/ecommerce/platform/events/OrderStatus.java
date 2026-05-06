package com.ecommerce.platform.events;

public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    FULFILLMENT_CONFIRMED,
    DELIVERY_SCHEDULED,
    COMPLETED,
    CANCELLED
}
