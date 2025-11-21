package com.tikam.microservices.inventory_service.service;

import com.tikam.microservices.inventory_service.dto.InventoryRecord;
import com.tikam.microservices.inventory_service.entity.Inventory;
import org.springframework.stereotype.Service;

public interface Inventory_service {
    Inventory createInventory(InventoryRecord inventoryRecord);
    boolean isInStock(String skuCode, Integer quantity);
    boolean placeOrder(String skuCode);
}
