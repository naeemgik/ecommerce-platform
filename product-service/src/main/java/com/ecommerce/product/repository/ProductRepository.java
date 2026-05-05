package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	@Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
	Optional<Product> findActiveById(@Param("id") Long id);

	Optional<Product> findBySkuAndActiveTrue(String sku);

	Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
	Page<Product> searchByName(@Param("name") String name, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
	List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

	@Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
	List<Product> findLowStockProducts(@Param("threshold") int threshold);

	@Modifying
	@Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
	int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

	@Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
	List<String> findAllActiveCategories();

	@Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.active = true GROUP BY p.category")
	List<Object[]> countByCategory();
}
