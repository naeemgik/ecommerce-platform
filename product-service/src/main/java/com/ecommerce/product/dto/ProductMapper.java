package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "inStock", expression = "java(product.getStockQuantity() != null && product.getStockQuantity() > 0)")
    @Mapping(target = "cacheStatus", ignore = true)
    @Mapping(source = "dimensions", target = "dimensions")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "dimensions", target = "dimensions")
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);

    @Mapping(source = "lengthCm", target = "lengthCm")
    @Mapping(source = "widthCm", target = "widthCm")
    @Mapping(source = "heightCm", target = "heightCm")
    ProductResponse.DimensionsDto toDimensionsDto(Product.ProductDimensions dimensions);

    @Mapping(source = "lengthCm", target = "lengthCm")
    @Mapping(source = "widthCm", target = "widthCm")
    @Mapping(source = "heightCm", target = "heightCm")
    Product.ProductDimensions toDimensionsEntity(ProductRequest.DimensionsRequest dimensions);
}
