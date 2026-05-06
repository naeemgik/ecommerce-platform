package com.ecommerce.platform.order.web;

import com.ecommerce.platform.order.service.OrderSagaService;
import com.ecommerce.platform.order.web.dto.CreateOrderRequest;
import com.ecommerce.platform.order.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderSagaService orderSagaService;

    public OrderController(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderSagaService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderSagaService.getOrder(orderId);
    }
}
