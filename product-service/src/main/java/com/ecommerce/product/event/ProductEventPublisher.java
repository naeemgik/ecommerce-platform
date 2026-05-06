package com.ecommerce.product.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.ecommerce.product.config.KafkaConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @Async("productTaskExecutor")
    public CompletableFuture<Void> publishProductCreated(Long productId, String sku, String name, String category) {
        ProductEvent event = ProductEvent.ofCreated(productId, sku, name, category);
        return publishEvent(PRODUCT_CREATED_TOPIC, String.valueOf(productId), event);
    }

    @Async("productTaskExecutor")
    public CompletableFuture<Void> publishProductUpdated(Long productId, String sku, String name, String category) {
        ProductEvent event = ProductEvent.ofUpdated(productId, sku, name, category);
        // Also publish cache invalidation
        publishEvent(PRODUCT_CACHE_INVALIDATION_TOPIC, String.valueOf(productId), ProductEvent.ofCacheInvalidation(productId));
        return publishEvent(PRODUCT_UPDATED_TOPIC, String.valueOf(productId), event);
    }

    @Async("productTaskExecutor")
    public CompletableFuture<Void> publishProductDeleted(Long productId, String sku) {
        ProductEvent event = ProductEvent.ofDeleted(productId, sku);
        publishEvent(PRODUCT_CACHE_INVALIDATION_TOPIC, String.valueOf(productId), ProductEvent.ofCacheInvalidation(productId));
        return publishEvent(PRODUCT_DELETED_TOPIC, String.valueOf(productId), event);
    }

    @Async("productTaskExecutor")
    public CompletableFuture<Void> publishProductViewed(Long productId) {
        ProductEvent event = ProductEvent.ofViewed(productId);
        return publishEvent(PRODUCT_VIEWED_TOPIC, String.valueOf(productId), event);
    }

    private CompletableFuture<Void> publishEvent(String topic, String key, ProductEvent event) {
        return kafkaTemplate.send(topic, key, event)
                .thenAccept((SendResult<String, ProductEvent> result) -> {
                    log.debug("Event published to topic '{}', partition: {}, offset: {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                })
                .exceptionally(ex -> {
                    log.error("Failed to publish event to topic '{}' for key '{}': {}",
                            topic, key, ex.getMessage(), ex);
                    return null;
                });
    }
}
