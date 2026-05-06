package com.ecommerce.platform.inventory.service;

import com.ecommerce.platform.events.InventoryRejectedEvent;
import com.ecommerce.platform.events.InventoryReservedEvent;
import com.ecommerce.platform.events.KafkaTopics;
import com.ecommerce.platform.events.OrderCancelledEvent;
import com.ecommerce.platform.events.OrderCreatedEvent;
import com.ecommerce.platform.inventory.domain.InventoryItem;
import com.ecommerce.platform.inventory.repository.InventoryItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventorySagaService {

    private final InventoryItemRepository inventoryItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventorySagaService(InventoryItemRepository inventoryItemRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service")
    public void reserveInventory(OrderCreatedEvent event) {
        boolean allAvailable = event.items().stream().allMatch(item -> inventoryItemRepository.findByProductId(item.productId())
                .map(inventory -> inventory.getAvailableQuantity() >= item.quantity())
                .orElse(false));

        if (!allAvailable) {
            kafkaTemplate.send(KafkaTopics.INVENTORY_REJECTED,
                    new InventoryRejectedEvent(event.orderId(), event.customerId(), "Inventory not available for one or more products"));
            return;
        }

        event.items().forEach(item -> inventoryItemRepository.findByProductId(item.productId()).ifPresent(inventory -> inventory.reserve(item.quantity())));
        kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVED,
                new InventoryReservedEvent(event.orderId(), event.customerId(), event.items(), event.totalAmount()));
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "inventory-compensation")
    public void releaseInventory(OrderCancelledEvent event) {
        // Compensation hook: in a fuller implementation we'd restore from a reservation ledger.
    }

    @Configuration
    static class InventorySeedConfig {
        @Bean
        CommandLineRunner seedInventory(InventoryItemRepository repository) {
            return args -> {
                if (repository.count() > 0) {
                    return;
                }
                repository.save(new InventoryItem(1L, 25));
                repository.save(new InventoryItem(2L, 100));
                repository.save(new InventoryItem(3L, 50));
            };
        }
    }
}
