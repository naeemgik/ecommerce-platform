package com.ecommerce.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"description"})
@EqualsAndHashCode(of = "id")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Embedded
    private ProductDimensions dimensions;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDimensions implements Serializable {
        @Column(name = "length_cm")
        private Double lengthCm;

        @Column(name = "width_cm")
        private Double widthCm;

        @Column(name = "height_cm")
        private Double heightCm;
    }
}
