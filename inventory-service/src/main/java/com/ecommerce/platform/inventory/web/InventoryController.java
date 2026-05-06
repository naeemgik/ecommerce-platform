package com.ecommerce.platform.inventory.web;

import com.ecommerce.platform.inventory.domain.InventoryItem;
import com.ecommerce.platform.inventory.repository.InventoryItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryController(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @GetMapping
    public List<InventoryItem> findAll() {
        return inventoryItemRepository.findAll();
    }
}
