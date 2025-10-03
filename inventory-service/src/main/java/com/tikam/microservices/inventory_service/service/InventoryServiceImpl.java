package com.tikam.microservices.inventory_service.service;

import com.tikam.microservices.inventory_service.dto.InventoryRecord;
import com.tikam.microservices.inventory_service.entity.Inventory;
import com.tikam.microservices.inventory_service.respository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements Inventory_service{
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory createInventory(InventoryRecord inventoryRecord) {
        Inventory inventory = new Inventory(inventoryRecord.skuCode(), inventoryRecord.quantity());
        return inventoryRepository.save(inventory);
    }

    @Override
    public boolean isInStock(String skuCode, Integer quantity) {
        return  inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }
}
