package com.ecommerce.platform.fulfillment.repository;

import com.ecommerce.platform.fulfillment.domain.FulfillmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentRecordRepository extends JpaRepository<FulfillmentRecord, Long> {
}
