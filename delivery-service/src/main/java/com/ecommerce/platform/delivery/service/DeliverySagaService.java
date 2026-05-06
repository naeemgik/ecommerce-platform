package com.ecommerce.platform.delivery.service;

import com.ecommerce.platform.delivery.domain.DeliveryRecord;
import com.ecommerce.platform.delivery.repository.DeliveryRecordRepository;
import com.ecommerce.platform.events.DeliveryScheduledEvent;
import com.ecommerce.platform.events.DeliveryStatus;
import com.ecommerce.platform.events.FulfillmentConfirmedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliverySagaService {

    private final DeliveryRecordRepository deliveryRecordRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliverySagaService(DeliveryRecordRepository deliveryRecordRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.deliveryRecordRepository = deliveryRecordRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.FULFILLMENT_CONFIRMED, groupId = "delivery-service")
    public void scheduleDelivery(FulfillmentConfirmedEvent event) {
        String reference = "DLV-%d".formatted(event.orderId());
        deliveryRecordRepository.save(new DeliveryRecord(event.orderId(), event.customerId(), reference, DeliveryStatus.SCHEDULED));
        kafkaTemplate.send(KafkaTopics.DELIVERY_SCHEDULED,
                new DeliveryScheduledEvent(event.orderId(), event.customerId(), reference));
    }
}
