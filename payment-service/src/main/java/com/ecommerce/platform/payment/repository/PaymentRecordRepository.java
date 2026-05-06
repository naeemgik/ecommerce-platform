package com.ecommerce.platform.payment.repository;

import com.ecommerce.platform.payment.domain.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
}
