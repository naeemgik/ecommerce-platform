package com.ecommerce.platform.fulfillment.web;

import com.ecommerce.platform.fulfillment.domain.FulfillmentRecord;
import com.ecommerce.platform.fulfillment.repository.FulfillmentRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentController {

    private final FulfillmentRecordRepository fulfillmentRecordRepository;

    public FulfillmentController(FulfillmentRecordRepository fulfillmentRecordRepository) {
        this.fulfillmentRecordRepository = fulfillmentRecordRepository;
    }

    @GetMapping
    public List<FulfillmentRecord> findAll() {
        return fulfillmentRecordRepository.findAll();
    }
}
