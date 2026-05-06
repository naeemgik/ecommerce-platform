package com.ecommerce.platform.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    protected NotificationLog() {
    }

    public NotificationLog(Long orderId, String eventType, String message) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
