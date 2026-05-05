package com.ecommerce.product.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent implements Serializable {

    public enum EventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED,
        PRODUCT_VIEWED,
        CACHE_INVALIDATION,
        STOCK_DEPLETED,
        LOW_STOCK_ALERT
    }

    private String eventId;
    private EventType eventType;
    private Long productId;
    private String productSku;
    private String productName;
    private String category;
    private Object payload; // Flexible payload for event-specific data
    private String source;
    private String correlationId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private String version;

    public static ProductEvent ofCreated(Long productId, String sku, String name, String category) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EventType.PRODUCT_CREATED)
                .productId(productId)
                .productSku(sku)
                .productName(name)
                .category(category)
                .source("product-service")
                .version("1.0")
                .build();
    }

    public static ProductEvent ofUpdated(Long productId, String sku, String name, String category) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EventType.PRODUCT_UPDATED)
                .productId(productId)
                .productSku(sku)
                .productName(name)
                .category(category)
                .source("product-service")
                .version("1.0")
                .build();
    }

    public static ProductEvent ofDeleted(Long productId, String sku) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EventType.PRODUCT_DELETED)
                .productId(productId)
                .productSku(sku)
                .source("product-service")
                .version("1.0")
                .build();
    }

    public static ProductEvent ofViewed(Long productId) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EventType.PRODUCT_VIEWED)
                .productId(productId)
                .source("product-service")
                .version("1.0")
                .build();
    }

    public static ProductEvent ofCacheInvalidation(Long productId) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EventType.CACHE_INVALIDATION)
                .productId(productId)
                .source("product-service")
                .version("1.0")
                .build();
    }
}
