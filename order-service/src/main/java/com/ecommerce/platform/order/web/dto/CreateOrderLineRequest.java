package com.ecommerce.platform.order.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderLineRequest(
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
