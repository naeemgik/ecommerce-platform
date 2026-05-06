package com.ecommerce.platform.order.domain;

import com.ecommerce.platform.events.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public PurchaseOrder(Long customerId, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void addItem(PurchaseOrderItem item) {
        item.attachTo(this);
        items.add(item);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void markFailed(OrderStatus status, String reason) {
        this.status = status;
        this.failureReason = reason;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }
}
