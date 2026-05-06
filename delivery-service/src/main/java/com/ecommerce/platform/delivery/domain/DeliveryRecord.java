package com.ecommerce.platform.delivery.domain;

import com.ecommerce.platform.events.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class DeliveryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String deliveryReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private Instant scheduledAt;

    protected DeliveryRecord() {
    }

    public DeliveryRecord(Long orderId, Long customerId, String deliveryReference, DeliveryStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.deliveryReference = deliveryReference;
        this.status = status;
        this.scheduledAt = Instant.now();
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

    public String getDeliveryReference() {
        return deliveryReference;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }
}
