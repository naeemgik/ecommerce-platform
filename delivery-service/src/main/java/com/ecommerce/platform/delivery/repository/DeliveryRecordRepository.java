package com.ecommerce.platform.delivery.repository;

import com.ecommerce.platform.delivery.domain.DeliveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRecordRepository extends JpaRepository<DeliveryRecord, Long> {
}
