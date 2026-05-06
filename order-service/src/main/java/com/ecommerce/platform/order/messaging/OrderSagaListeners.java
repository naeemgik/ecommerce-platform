package com.ecommerce.platform.order.messaging;

import com.ecommerce.platform.events.DeliveryScheduledEvent;
import com.ecommerce.platform.events.FulfillmentConfirmedEvent;
import com.ecommerce.platform.events.InventoryRejectedEvent;
import com.ecommerce.platform.events.InventoryReservedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.PaymentFailedEvent;
import com.ecommerce.platform.events.PaymentProcessedEvent;
import com.ecommerce.platform.order.service.OrderSagaService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaListeners {

    private final OrderSagaService orderSagaService;

    public OrderSagaListeners(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "order-service")
    public void onInventoryReserved(InventoryReservedEvent event) {
        orderSagaService.markInventoryReserved(event);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_REJECTED, groupId = "order-service")
    public void onInventoryRejected(InventoryRejectedEvent event) {
        orderSagaService.markInventoryFailed(event);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "order-service")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        orderSagaService.markPaymentCompleted(event);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderSagaService.markPaymentFailed(event);
    }

    @KafkaListener(topics = KafkaTopics.FULFILLMENT_CONFIRMED, groupId = "order-service")
    public void onFulfillmentConfirmed(FulfillmentConfirmedEvent event) {
        orderSagaService.markFulfillmentConfirmed(event);
    }

    @KafkaListener(topics = KafkaTopics.DELIVERY_SCHEDULED, groupId = "order-service")
    public void onDeliveryScheduled(DeliveryScheduledEvent event) {
        orderSagaService.markDeliveryScheduled(event);
    }
}
