package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ApiResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductController {

	private final ProductService productService;

	@GetMapping("/{id}")
	@Timed(value = "api.product.get.time", description = "Time to retrieve a product")
	public CompletableFuture<ResponseEntity<ApiResponse<ProductResponse>>> getProductById(
			@PathVariable @Positive(message = "Product ID must be positive") Long id) {

		log.debug("Received request for product ID: {}", id);

		return productService.getProductByIdAsync(id).thenApply(product -> {
			log.debug("Returning product ID: {} (cache: {})", id, product.getCacheStatus());
			return ResponseEntity.ok(ApiResponse.success(product));
		}).exceptionally(ex -> {
			Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
			log.error("Error fetching product ID {}: {}", id, cause.getMessage());

			if (cause.getMessage() != null && cause.getMessage().contains("not found")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(ApiResponse.error(cause.getMessage(), "PRODUCT_NOT_FOUND"));
			}
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
					ApiResponse.error("Service temporarily unavailable. Please try again.", "SERVICE_UNAVAILABLE"));
		});
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
			@RequestParam(required = false) String category, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {

		if (size > 100)
			size = 100; // Cap page size
		Pageable pageable = PageRequest.of(page, size,
				sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

		Page<ProductResponse> products = (category != null && !category.isBlank())
				? productService.getProductsByCategory(category, pageable)
				: productService.getProductsByCategory("", pageable);

		return ResponseEntity.ok(ApiResponse.success(products));
	}

	@GetMapping("/search")
	public CompletableFuture<ResponseEntity<ApiResponse<Page<ProductResponse>>>> searchProducts(
			@RequestParam String name, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		return productService.searchProductsAsync(name, pageable)
				.thenApply(results -> ResponseEntity.ok(ApiResponse.success(results)));
	}

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
		List<String> categories = productService.getAllCategories();
		return ResponseEntity.ok(ApiResponse.success(categories));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {

		log.info("Creating product with SKU: {}", request.getSku());
		ProductResponse created = productService.createProduct(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(created, "Product created successfully"));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable @Positive Long id,
			@Valid @RequestBody ProductRequest request) {

		log.info("Updating product ID: {}", id);
		ProductResponse updated = productService.updateProduct(id, request);
		return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable @Positive Long id) {

		log.info("Deleting product ID: {}", id);
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}
}
