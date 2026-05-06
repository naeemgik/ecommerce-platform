package com.ecommerce.platform.delivery.web;

import com.ecommerce.platform.delivery.domain.DeliveryRecord;
import com.ecommerce.platform.delivery.repository.DeliveryRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryRecordRepository deliveryRecordRepository;

    public DeliveryController(DeliveryRecordRepository deliveryRecordRepository) {
        this.deliveryRecordRepository = deliveryRecordRepository;
    }

    @GetMapping
    public List<DeliveryRecord> findAll() {
        return deliveryRecordRepository.findAll();
    }
}
