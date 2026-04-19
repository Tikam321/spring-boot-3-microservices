package com.tikam.microservices.inventory_service.service;

import com.tikam.microservices.order_service.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaListener.class);
    private final Inventory_service inventoryService;

    public InventoryKafkaListener(Inventory_service inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        System.out.println("the kafka listner for the order-placed envent " + orderPlacedEvent);
        log.info("Got message from order-placed topic {}", orderPlacedEvent);
        inventoryService.handleOrderPlacedEvent(orderPlacedEvent);
    }
}
