package com.ecommerce.platform.fulfillment.domain;

import com.ecommerce.platform.events.FulfillmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class FulfillmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String referenceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected FulfillmentRecord() {
    }

    public FulfillmentRecord(Long orderId, Long customerId, String referenceCode, FulfillmentStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.referenceCode = referenceCode;
        this.status = status;
        this.createdAt = Instant.now();
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

    public String getReferenceCode() {
        return referenceCode;
    }

    public FulfillmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
