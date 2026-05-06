package com.ecommerce.platform.product.repository;

import com.ecommerce.platform.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
