package com.ecommerce.platform.order.web.dto;

import com.ecommerce.platform.events.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String failureReason,
        Instant createdAt,
        List<OrderItemResponse> items
) {
}
