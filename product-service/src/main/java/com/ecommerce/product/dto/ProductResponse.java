package com.ecommerce.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String sku;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stockQuantity;
	private String category;
	private String brand;
	private String imageUrl;
	private BigDecimal weightKg;
	private Boolean active;
	private DimensionsDto dimensions;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	// Computed field - not stored in DB
	private Boolean inStock;
	private String cacheStatus; // "HIT" or "MISS" for debugging

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DimensionsDto implements Serializable {
		private Double lengthCm;
		private Double widthCm;
		private Double heightCm;
	}
}

// -------------------------------------------------------

// ProductCreateRequest.java (inner class alternative, but separate for clarity)
