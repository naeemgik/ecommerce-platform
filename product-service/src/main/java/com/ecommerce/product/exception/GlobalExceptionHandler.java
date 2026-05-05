package com.ecommerce.product.exception;

import com.ecommerce.product.dto.ApiResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException ex, WebRequest request) {
		log.warn("Product not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error(ex.getMessage(), "PRODUCT_NOT_FOUND"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});
		log.warn("Validation failed: {}", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Map<String, String>>builder()
				.success(false).message("Validation failed").data(errors).errorCode("VALIDATION_ERROR").build());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		log.error("Data integrity violation: {}", ex.getMessage());
		String message = ex.getMessage() != null && ex.getMessage().contains("sku")
				? "A product with this SKU already exists"
				: "Data integrity violation";
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(message, "DATA_CONFLICT"));
	}

	@ExceptionHandler(CallNotPermittedException.class)
	public ResponseEntity<ApiResponse<Void>> handleCircuitBreakerOpen(CallNotPermittedException ex) {
		log.warn("Circuit breaker is OPEN: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiResponse.error(
						"Service is temporarily unavailable due to high error rate. Please try again later.",
						"CIRCUIT_BREAKER_OPEN"));
	}

	@ExceptionHandler(BulkheadFullException.class)
	public ResponseEntity<ApiResponse<Void>> handleBulkheadFull(BulkheadFullException ex) {
		log.warn("Bulkhead is full: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse
				.error("Server is currently handling too many requests. Please try again later.", "BULKHEAD_FULL"));
	}

	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RequestNotPermitted ex) {
		log.warn("Rate limit exceeded: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(ApiResponse.error("Rate limit exceeded. Please slow down.", "RATE_LIMIT_EXCEEDED"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
		log.error("Unexpected error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
	}
}
