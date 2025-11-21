package com.tikam.microservices.inventory_service.service;

import com.tikam.microservices.inventory_service.dto.InventoryRecord;
import com.tikam.microservices.inventory_service.entity.Inventory;
import com.tikam.microservices.inventory_service.respository.InventoryRepository;
import jakarta.transaction.Transactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class InventoryServiceImpl implements Inventory_service{
    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, RedissonClient redissonClient) {
        this.inventoryRepository = inventoryRepository;
        this.redissonClient = redissonClient;
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

    @Transactional
    public boolean placeOrder(String skuCode) {
        boolean orderPlaced = false;
        RLock lock = redissonClient.getLock("inventory-lock-" + skuCode);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Inventory inv = inventoryRepository.findFirstBySkuCode(skuCode)
                        .orElseThrow(() -> new RuntimeException("Item not found"));
                if (inv.getQuantity() <= 0) throw new RuntimeException("Out of stock");
                inv.setQuantity(inv.getQuantity() - 1);
                if (inv.getQuantity() <= 0) {
                    inventoryRepository.delete(inv);
                } else {
                    inventoryRepository.save(inv);
                }
                orderPlaced = true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
        return orderPlaced;
    }

    }
