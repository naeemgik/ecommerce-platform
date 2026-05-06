package com.ecommerce.product.event;

import com.ecommerce.product.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.ecommerce.product.config.KafkaConfig.PRODUCT_CACHE_INVALIDATION_TOPIC;
import static com.ecommerce.product.config.KafkaConfig.PRODUCT_VIEWED_TOPIC;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final CacheManager cacheManager;

    /**
     * Listens for cache invalidation events - triggered when product is updated/deleted
     * in any service instance. Ensures distributed cache consistency.
     */
    @KafkaListener(
            topics = PRODUCT_CACHE_INVALIDATION_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:product-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCacheInvalidation(ConsumerRecord<String, ProductEvent> record, Acknowledgment ack) {
        ProductEvent event = record.value();
        try {
            if (event != null && event.getProductId() != null) {
                log.info("Received cache invalidation event for product ID: {}", event.getProductId());
                evictProductCache(event.getProductId());
                log.debug("Cache evicted for product ID: {}", event.getProductId());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing cache invalidation event: {}", e.getMessage(), e);
            ack.acknowledge(); // Acknowledge anyway to avoid infinite loop on bad messages
        }
    }

    /**
     * Listens for product view events - useful for analytics and trending products
     */
    @KafkaListener(
            topics = PRODUCT_VIEWED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:product-service-group}-analytics",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProductViewed(ConsumerRecord<String, ProductEvent> record, Acknowledgment ack) {
        ProductEvent event = record.value();
        try {
            if (event != null && event.getProductId() != null) {
                // Here you could increment view count in a separate analytics store
                log.debug("Product viewed event received for product ID: {}", event.getProductId());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing product view event: {}", e.getMessage(), e);
            ack.acknowledge();
        }
    }

    private void evictProductCache(Long productId) {
        var productCache = cacheManager.getCache(RedisConfig.PRODUCT_CACHE);
        if (productCache != null) {
            productCache.evict(productId);
        }
        // Also evict list caches since they may include this product
        var listCache = cacheManager.getCache(RedisConfig.PRODUCT_LIST_CACHE);
        if (listCache != null) {
            Objects.requireNonNull(listCache).clear();
        }
    }
}
