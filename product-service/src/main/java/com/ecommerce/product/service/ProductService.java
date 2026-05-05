package com.ecommerce.product.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.product.config.RedisConfig;
import com.ecommerce.product.dto.ProductMapper;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.repository.ProductRepository;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

	private static final String RESILIENCE4J_CB = "productService";
	private static final String PRODUCT_CACHE_KEY_PREFIX = "product:";

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;
	private final ProductEventPublisher eventPublisher;
	private final RedisTemplate<String, ProductResponse> productRedisTemplate;
	private final MeterRegistry meterRegistry;

	@Async("productTaskExecutor")
	@Timed(value = "product.fetch.time", description = "Time taken to fetch product")
	public CompletableFuture<ProductResponse> getProductByIdAsync(Long id) {
		log.debug("Fetching product asynchronously for ID: {}", id);

		String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;

		ProductResponse cached = getFromCache(cacheKey);
		if (cached != null) {
			log.debug("Cache HIT for product ID: {}", id);
			cached.setCacheStatus("HIT");
			meterRegistry.counter("product.cache.hits").increment();
			// Publish view event asynchronously (non-blocking analytics)
			eventPublisher.publishProductViewed(id);
			return CompletableFuture.completedFuture(cached);
		}

		log.debug("Cache MISS for product ID: {}, fetching from database", id);
		meterRegistry.counter("product.cache.misses").increment();

		ProductResponse product = fetchProductFromDatabase(id);
		product.setCacheStatus("MISS");

		storeInCache(cacheKey, product);

		eventPublisher.publishProductViewed(id);

		return CompletableFuture.completedFuture(product);
	}

	@Cacheable(value = RedisConfig.PRODUCT_CACHE, key = "#id", unless = "#result == null")
	@CircuitBreaker(name = RESILIENCE4J_CB, fallbackMethod = "getProductFallback")
	@Retry(name = RESILIENCE4J_CB)
	public ProductResponse getProductById(Long id) {
		return fetchProductFromDatabase(id);
	}

	@Cacheable(value = RedisConfig.PRODUCT_LIST_CACHE, key = "#category + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	@CircuitBreaker(name = RESILIENCE4J_CB)
	@RateLimiter(name = RESILIENCE4J_CB)
	public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
		log.debug("Fetching products for category: {}", category);
		return productRepository.findByCategoryAndActiveTrue(category, pageable).map(productMapper::toResponse);
	}

	@Async("productTaskExecutor")
	@Bulkhead(name = RESILIENCE4J_CB, type = Bulkhead.Type.THREADPOOL)
	@Timed(value = "product.search.time", description = "Time taken to search products")
	public CompletableFuture<Page<ProductResponse>> searchProductsAsync(String name, Pageable pageable) {
		log.debug("Searching products with name: {}", name);
		Page<ProductResponse> results = productRepository.searchByName(name, pageable).map(productMapper::toResponse);
		return CompletableFuture.completedFuture(results);
	}

	@Cacheable(value = RedisConfig.CATEGORY_CACHE, key = "'all-categories'")
	@CircuitBreaker(name = RESILIENCE4J_CB)
	public List<String> getAllCategories() {
		return productRepository.findAllActiveCategories();
	}

	@Transactional
	@CachePut(value = RedisConfig.PRODUCT_CACHE, key = "#result.id")
	@CacheEvict(value = RedisConfig.PRODUCT_LIST_CACHE, allEntries = true)
	@Timed(value = "product.create.time")
	public ProductResponse createProduct(ProductRequest request) {
		log.info("Creating new product with SKU: {}", request.getSku());
		Product product = productMapper.toEntity(request);
		Product savedProduct = productRepository.save(product);
		ProductResponse response = productMapper.toResponse(savedProduct);

		// Publish event asynchronously
		eventPublisher.publishProductCreated(savedProduct.getId(), savedProduct.getSku(), savedProduct.getName(),
				savedProduct.getCategory());

		log.info("Product created with ID: {}", savedProduct.getId());
		return response;
	}

	@Transactional
	@CachePut(value = RedisConfig.PRODUCT_CACHE, key = "#id")
	@CacheEvict(value = RedisConfig.PRODUCT_LIST_CACHE, allEntries = true)
	@Timed(value = "product.update.time")
	public ProductResponse updateProduct(Long id, ProductRequest request) {
		log.info("Updating product ID: {}", id);
		Product existingProduct = productRepository.findActiveById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

		productMapper.updateEntityFromRequest(request, existingProduct);
		Product updatedProduct = productRepository.save(existingProduct);
		ProductResponse response = productMapper.toResponse(updatedProduct);

		eventPublisher.publishProductUpdated(updatedProduct.getId(), updatedProduct.getSku(), updatedProduct.getName(),
				updatedProduct.getCategory());

		return response;
	}

	@Transactional
	@CacheEvict(value = { RedisConfig.PRODUCT_CACHE, RedisConfig.PRODUCT_LIST_CACHE }, key = "#id", allEntries = false)
	@Timed(value = "product.delete.time")
	public void deleteProduct(Long id) {
		log.info("Soft deleting product ID: {}", id);
		Product product = productRepository.findActiveById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

		product.setActive(false);
		productRepository.save(product);

		productRedisTemplate.delete(PRODUCT_CACHE_KEY_PREFIX + id);

		eventPublisher.publishProductDeleted(product.getId(), product.getSku());
	}

	@CircuitBreaker(name = RESILIENCE4J_CB, fallbackMethod = "getProductFallback")
	@Retry(name = RESILIENCE4J_CB)
	@Bulkhead(name = RESILIENCE4J_CB)
	private ProductResponse fetchProductFromDatabase(Long id) {
		log.debug("Fetching product from database for ID: {}", id);
		return productRepository.findActiveById(id).map(productMapper::toResponse)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
	}

	private ProductResponse getFromCache(String key) {
		try {
			return productRedisTemplate.opsForValue().get(key);
		} catch (Exception e) {
			log.warn("Redis cache read error for key '{}': {}. Falling back to database.", key, e.getMessage());
			meterRegistry.counter("product.cache.errors", "type", "read").increment();
			return null;
		}
	}

	private void storeInCache(String key, ProductResponse product) {
		try {
			productRedisTemplate.opsForValue().set(key, product, RedisConfig.PRODUCT_CACHE_TTL);
			log.debug("Stored product in cache with key '{}' and TTL: {}", key, RedisConfig.PRODUCT_CACHE_TTL);
		} catch (Exception e) {
			log.warn("Redis cache write error for key '{}': {}. Data served from database.", key, e.getMessage());
			meterRegistry.counter("product.cache.errors", "type", "write").increment();
		}
	}

	public ProductResponse getProductFallback(Long id, Throwable throwable) {
		log.error("Circuit breaker fallback triggered for product ID: {} - Reason: {}", id, throwable.getMessage());
		meterRegistry.counter("product.circuit.breaker.fallback").increment();

		if (throwable instanceof ProductNotFoundException) {
			throw (ProductNotFoundException) throwable;
		}

		throw new RuntimeException("Product service temporarily unavailable. Please try again later.", throwable);
	}

	public CompletableFuture<ProductResponse> getProductAsyncFallback(Long id, Throwable throwable) {
		return CompletableFuture
				.failedFuture(new RuntimeException("Product service temporarily unavailable.", throwable));
	}
}
