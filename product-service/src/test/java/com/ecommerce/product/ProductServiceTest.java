package com.ecommerce.product;

import com.ecommerce.product.config.RedisConfig;
import com.ecommerce.product.dto.ProductMapper;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductEventPublisher eventPublisher;

    @Mock
    private RedisTemplate<String, ProductResponse> productRedisTemplate;

    @Mock
    private ValueOperations<String, ProductResponse> valueOperations;

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductResponse testProductResponse;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .brand("TestBrand")
                .active(true)
                .build();

        testProductResponse = ProductResponse.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .category("Electronics")
                .inStock(true)
                .build();

        when(productRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("getProductByIdAsync - should return cached product on cache HIT")
    void getProductByIdAsync_CacheHit_ReturnsCachedProduct() throws ExecutionException, InterruptedException {
        // Given
        when(valueOperations.get("product:1")).thenReturn(testProductResponse);
        when(eventPublisher.publishProductViewed(1L)).thenReturn(CompletableFuture.completedFuture(null));

        // When
        CompletableFuture<ProductResponse> future = productService.getProductByIdAsync(1L);
        ProductResponse result = future.get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCacheStatus()).isEqualTo("HIT");

        // Verify DB was NOT called
        verify(productRepository, never()).findActiveById(anyLong());
        verify(valueOperations).get("product:1");
    }

    @Test
    @DisplayName("getProductByIdAsync - should fetch from DB and populate cache on cache MISS")
    void getProductByIdAsync_CacheMiss_FetchesFromDBAndCaches() throws ExecutionException, InterruptedException {
        // Given
        when(valueOperations.get("product:1")).thenReturn(null); // Cache MISS
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);
        when(eventPublisher.publishProductViewed(1L)).thenReturn(CompletableFuture.completedFuture(null));

        // When
        CompletableFuture<ProductResponse> future = productService.getProductByIdAsync(1L);
        ProductResponse result = future.get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCacheStatus()).isEqualTo("MISS");

        // Verify DB was called
        verify(productRepository).findActiveById(1L);

        // Verify cache was populated
        verify(valueOperations).set(eq("product:1"), eq(testProductResponse), eq(RedisConfig.PRODUCT_CACHE_TTL));
    }

    @Test
    @DisplayName("getProductByIdAsync - should throw ProductNotFoundException when product not found")
    void getProductByIdAsync_ProductNotFound_ThrowsException() {
        // Given
        when(valueOperations.get("product:999")).thenReturn(null);
        when(productRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When
        CompletableFuture<ProductResponse> future = productService.getProductByIdAsync(999L);

        // Then
        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("createProduct - should save product, return response, and publish event")
    void createProduct_ValidRequest_SavesAndPublishesEvent() {
        // Given
        ProductRequest request = ProductRequest.builder()
                .sku("NEW-001")
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .stockQuantity(200)
                .category("Clothing")
                .build();

        when(productMapper.toEntity(request)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);
        when(eventPublisher.publishProductCreated(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        ProductResponse result = productService.createProduct(request);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(testProduct);
        verify(eventPublisher).publishProductCreated(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getProductByIdAsync - should still serve data when Redis is down (graceful degradation)")
    void getProductByIdAsync_RedisFails_FallsBackToDatabase() throws ExecutionException, InterruptedException {
        // Given - Redis throws exception
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection refused"));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);
        when(eventPublisher.publishProductViewed(1L)).thenReturn(CompletableFuture.completedFuture(null));
        doThrow(new RuntimeException("Redis connection refused")).when(valueOperations)
                .set(anyString(), any(), any());

        // When
        CompletableFuture<ProductResponse> future = productService.getProductByIdAsync(1L);
        ProductResponse result = future.get();

        // Then - should still return data despite Redis failure
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).findActiveById(1L);
    }
}
