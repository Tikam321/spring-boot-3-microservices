package com.tikam.microservices.inventory_service.service;

import com.tikam.microservices.inventory_service.dto.InventoryRecord;
import com.tikam.microservices.inventory_service.entity.Inventory;
import com.tikam.microservices.inventory_service.event.InventoryReservedEvent;
import com.tikam.microservices.inventory_service.event.InventoryReservationFailedEvent;
import com.tikam.microservices.inventory_service.respository.InventoryRepository;
import com.tikam.microservices.order_service.event.OrderPlacedEvent;
import jakarta.transaction.Transactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class InventoryServiceImpl implements Inventory_service{
    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    public InventoryServiceImpl(InventoryRepository inventoryRepository, RedissonClient redissonClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.redissonClient = redissonClient;
        this.kafkaTemplate = kafkaTemplate;
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

    @Override
    @Transactional
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        RLock lock = redissonClient.getLock("inventory-lock-" + orderPlacedEvent.getSkuCode());
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Inventory inv = inventoryRepository.findFirstBySkuCode(orderPlacedEvent.getSkuCode().toString())
                        .orElse(null);

                if (inv != null && inv.getQuantity() > 0) {
                    inv.setQuantity(inv.getQuantity() - 1);
                    inventoryRepository.save(inv);

                    InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent();
                    inventoryReservedEvent.setOrderNumber(orderPlacedEvent.getOrderNumber());
                    kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
                    log.info("Inventory reserved for order {}", orderPlacedEvent.getOrderNumber());
                } else {
                    InventoryReservationFailedEvent inventoryReservationFailedEvent = new InventoryReservationFailedEvent();
                    inventoryReservationFailedEvent.setOrderNumber(orderPlacedEvent.getOrderNumber());
                    kafkaTemplate.send("inventory-reservation-failed", inventoryReservationFailedEvent);
                    log.info("Inventory reservation failed for order {}", orderPlacedEvent.getOrderNumber());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
