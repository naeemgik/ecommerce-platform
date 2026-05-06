package com.ecommerce.platform.product.service;

import com.ecommerce.platform.events.ProductView;
import com.ecommerce.platform.product.domain.Product;
import com.ecommerce.platform.product.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final ProductRepository productRepository;
    private final RedisTemplate<String, ProductView> redisTemplate;
    public ProductService(
            ProductRepository productRepository,
            RedisTemplate<String, ProductView> redisTemplate
    ) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    @Async("productLookupExecutor")
    public CompletableFuture<ProductView> getProductAsync(Long productId) {
        String cacheKey = cacheKey(productId);
        ProductView cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        ProductView productView = toView(product);
        redisTemplate.opsForValue().set(cacheKey, productView, CACHE_TTL);
        return CompletableFuture.completedFuture(productView);
    }

    public ProductView toView(Product product) {
        return new ProductView(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isAvailable()
        );
    }

    private String cacheKey(Long productId) {
        return "product:%d".formatted(productId);
    }
}
