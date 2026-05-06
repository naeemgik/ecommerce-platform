package com.ecommerce.platform.cart.service;

import com.ecommerce.platform.cart.domain.CartItem;
import com.ecommerce.platform.cart.repository.CartItemRepository;
import com.ecommerce.platform.cart.web.dto.AddCartItemRequest;
import com.ecommerce.platform.cart.web.dto.CartItemResponse;
import com.ecommerce.platform.events.ProductView;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final WebClient productServiceWebClient;

    public CartService(CartItemRepository cartItemRepository, WebClient productServiceWebClient) {
        this.cartItemRepository = cartItemRepository;
        this.productServiceWebClient = productServiceWebClient;
    }

    public List<CartItemResponse> getCart(Long customerId) {
        return cartItemRepository.findByCustomerId(customerId).stream()
                .map(item -> new CartItemResponse(item.getProductId(), lookupProduct(item.getProductId()).name(), item.getQuantity()))
                .toList();
    }

    public void addItem(Long customerId, AddCartItemRequest request) {
        lookupProduct(request.productId());
        cartItemRepository.save(new CartItem(customerId, request.productId(), request.quantity()));
    }

    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    @CircuitBreaker(name = "productCatalog", fallbackMethod = "fallbackProduct")
    public ProductView lookupProduct(Long productId) {
        return productServiceWebClient.get()
                .uri("/api/products/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductView.class)
                .block();
    }

    private ProductView fallbackProduct(Long productId, Throwable throwable) {
        throw new IllegalStateException("Unable to enrich cart item %d".formatted(productId), throwable);
    }
}
