package com.ecommerce.platform.events;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_REJECTED = "inventory.rejected";
    public static final String PAYMENT_PROCESSED = "payment.processed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String FULFILLMENT_CONFIRMED = "fulfillment.confirmed";
    public static final String DELIVERY_SCHEDULED = "delivery.scheduled";
    public static final String ORDER_CANCELLED = "order.cancelled";

    private KafkaTopics() {
    }
}
