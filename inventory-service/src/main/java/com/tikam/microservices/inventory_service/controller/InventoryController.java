package com.tikam.microservices.inventory_service.controller;

import com.tikam.microservices.inventory_service.dto.InventoryRecord;
import com.tikam.microservices.inventory_service.entity.Inventory;
import com.tikam.microservices.inventory_service.service.Inventory_service;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final Inventory_service inventory_service;

    public InventoryController(Inventory_service inventoryService) {
        inventory_service = inventoryService;
    }

    @GetMapping
    public boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        return inventory_service.isInStock(skuCode, quantity);
    }
}
