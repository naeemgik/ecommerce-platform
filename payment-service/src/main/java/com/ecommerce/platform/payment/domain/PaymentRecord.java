package com.ecommerce.platform.payment.domain;

import com.ecommerce.platform.events.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant processedAt;

    protected PaymentRecord() {
    }

    public PaymentRecord(Long orderId, Long customerId, BigDecimal amount, PaymentStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.processedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
