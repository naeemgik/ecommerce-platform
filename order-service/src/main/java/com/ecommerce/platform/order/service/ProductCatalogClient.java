package com.ecommerce.platform.order.service;

import com.ecommerce.platform.events.ProductView;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Component
public class ProductCatalogClient {

    private final WebClient productCatalogWebClient;

    public ProductCatalogClient(WebClient productCatalogWebClient) {
        this.productCatalogWebClient = productCatalogWebClient;
    }

    @Retry(name = "productCatalog")
    @CircuitBreaker(name = "productCatalog", fallbackMethod = "fallbackProductLookup")
    @TimeLimiter(name = "productCatalog")
    public CompletableFuture<ProductView> fetchProduct(Long productId) {
        return productCatalogWebClient.get()
                .uri("/api/products/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductView.class)
                .toFuture();
    }

    private CompletableFuture<ProductView> fallbackProductLookup(Long productId, Throwable throwable) {
        return CompletableFuture.failedFuture(
                new IllegalStateException("Product catalog unavailable for product %d".formatted(productId), throwable)
        );
    }
}
