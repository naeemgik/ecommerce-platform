package com.ecommerce.platform.payment.service;

import com.ecommerce.platform.events.InventoryReservedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.PaymentFailedEvent;
import com.ecommerce.platform.events.PaymentProcessedEvent;
import com.ecommerce.platform.events.PaymentStatus;
import com.ecommerce.platform.payment.domain.PaymentRecord;
import com.ecommerce.platform.payment.repository.PaymentRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentSagaService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentSagaService(PaymentRecordRepository paymentRecordRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "payment-service")
    public void processPayment(InventoryReservedEvent event) {
        boolean approved = event.totalAmount().doubleValue() < 5000;
        paymentRecordRepository.save(new PaymentRecord(
                event.orderId(),
                event.customerId(),
                event.totalAmount(),
                approved ? PaymentStatus.RECEIVED : PaymentStatus.FAILED
        ));

        if (approved) {
            kafkaTemplate.send(KafkaTopics.PAYMENT_PROCESSED,
                    new PaymentProcessedEvent(event.orderId(), event.customerId(), event.totalAmount()));
            return;
        }

        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED,
                new PaymentFailedEvent(event.orderId(), event.customerId(), "Payment rejected by gateway risk rules"));
    }
}
