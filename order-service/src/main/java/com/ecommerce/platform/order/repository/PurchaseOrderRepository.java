package com.ecommerce.platform.order.repository;

import com.ecommerce.platform.order.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<PurchaseOrder> findById(Long id);
}
