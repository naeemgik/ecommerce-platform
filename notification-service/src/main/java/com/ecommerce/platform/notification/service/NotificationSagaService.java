package com.ecommerce.platform.notification.service;

import com.ecommerce.platform.events.DeliveryScheduledEvent;
import com.ecommerce.platform.events.InventoryRejectedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.OrderCancelledEvent;
import com.ecommerce.platform.events.OrderCreatedEvent;
import com.ecommerce.platform.events.PaymentFailedEvent;
import com.ecommerce.platform.notification.domain.NotificationLog;
import com.ecommerce.platform.notification.repository.NotificationLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationSagaService {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationSagaService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "notification-created")
    public void onOrderCreated(OrderCreatedEvent event) {
        notificationLogRepository.save(new NotificationLog(event.orderId(), "ORDER_CREATED", "Order accepted for processing"));
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_REJECTED, groupId = "notification-inventory")
    public void onInventoryRejected(InventoryRejectedEvent event) {
        notificationLogRepository.save(new NotificationLog(event.orderId(), "INVENTORY_REJECTED", event.reason()));
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "notification-payment")
    public void onPaymentFailed(PaymentFailedEvent event) {
        notificationLogRepository.save(new NotificationLog(event.orderId(), "PAYMENT_FAILED", event.reason()));
    }

    @KafkaListener(topics = KafkaTopics.DELIVERY_SCHEDULED, groupId = "notification-delivery")
    public void onDeliveryScheduled(DeliveryScheduledEvent event) {
        notificationLogRepository.save(new NotificationLog(event.orderId(), "DELIVERY_SCHEDULED", "Delivery scheduled with ref " + event.deliveryReference()));
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "notification-cancelled")
    public void onOrderCancelled(OrderCancelledEvent event) {
        notificationLogRepository.save(new NotificationLog(event.orderId(), "ORDER_CANCELLED", event.reason()));
    }
}
