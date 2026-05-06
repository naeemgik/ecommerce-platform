package com.ecommerce.platform.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private int availableQuantity;

    protected InventoryItem() {
    }

    public InventoryItem(Long productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void reserve(int quantity) {
        this.availableQuantity -= quantity;
    }

    public void release(int quantity) {
        this.availableQuantity += quantity;
    }
}
