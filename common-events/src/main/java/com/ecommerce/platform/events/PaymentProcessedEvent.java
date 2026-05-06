package com.ecommerce.platform.events;

import java.math.BigDecimal;

public record PaymentProcessedEvent(
        Long orderId,
        Long customerId,
        BigDecimal amount
) {
}
