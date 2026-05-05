package com.ecommerce.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, digits, and hyphens")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999999.99", message = "Price is too high")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 999999, message = "Stock quantity is too high")
    private Integer stockQuantity;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @DecimalMin(value = "0.001", message = "Weight must be positive")
    private BigDecimal weightKg;

    @Valid
    private DimensionsRequest dimensions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionsRequest {
        @DecimalMin(value = "0.1")
        private Double lengthCm;

        @DecimalMin(value = "0.1")
        private Double widthCm;

        @DecimalMin(value = "0.1")
        private Double heightCm;
    }
}
