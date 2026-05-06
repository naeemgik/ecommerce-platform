package com.ecommerce.platform.payment.web;

import com.ecommerce.platform.payment.domain.PaymentRecord;
import com.ecommerce.platform.payment.repository.PaymentRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentController(PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @GetMapping
    public List<PaymentRecord> findAll() {
        return paymentRecordRepository.findAll();
    }
}
