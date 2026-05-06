package com.ecommerce.platform.fulfillment.service;

import com.ecommerce.platform.events.FulfillmentConfirmedEvent;
import com.ecommerce.platform.events.FulfillmentStatus;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.PaymentProcessedEvent;
import com.ecommerce.platform.fulfillment.domain.FulfillmentRecord;
import com.ecommerce.platform.fulfillment.repository.FulfillmentRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentSagaService {

    private final FulfillmentRecordRepository fulfillmentRecordRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FulfillmentSagaService(FulfillmentRecordRepository fulfillmentRecordRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.fulfillmentRecordRepository = fulfillmentRecordRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "fulfillment-service")
    public void confirmFulfillment(PaymentProcessedEvent event) {
        String reference = "FUL-%d".formatted(event.orderId());
        fulfillmentRecordRepository.save(new FulfillmentRecord(event.orderId(), event.customerId(), reference, FulfillmentStatus.CONFIRMED));
        kafkaTemplate.send(KafkaTopics.FULFILLMENT_CONFIRMED,
                new FulfillmentConfirmedEvent(event.orderId(), event.customerId(), reference));
    }
}
