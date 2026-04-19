package com.tikam.microservices.order_service.service;

import com.tikam.microservices.inventory_service.event.InventoryReservedEvent;
import com.tikam.microservices.inventory_service.event.InventoryReservationFailedEvent;
import com.tikam.microservices.order_service.entity.Order;
import com.tikam.microservices.order_service.event.OrderConfirmedEvent;
import com.tikam.microservices.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public OrderKafkaListener(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "inventory-reserved")
    public void handleInventoryReserved(InventoryReservedEvent inventoryReservedEvent) {
        log.info("Got message from inventory-reserved topic {}", inventoryReservedEvent);
        Order order = orderRepository.findById(Long.parseLong(inventoryReservedEvent.getOrderNumber().toString())).orElse(null);
        if (order != null) {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            log.info("Order status updated to CONFIRMED for order {}", order.getId());

            OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent();
            orderConfirmedEvent.setOrderNumber(order.getId().toString());
            orderConfirmedEvent.setEmail(order.getEmail());
            orderConfirmedEvent.setFirstName("tikam");
            orderConfirmedEvent.setLastName("suvasiya");

            kafkaTemplate.send("order-confirmed", orderConfirmedEvent);
            log.info("Sent OrderConfirmedEvent for order {}", order.getId());
        }
    }

    @KafkaListener(topics = "inventory-reservation-failed")
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent inventoryReservationFailedEvent) {
        log.info("Got message from inventory-reservation-failed topic {}", inventoryReservationFailedEvent);
        Order order = orderRepository.findById(Long.parseLong(inventoryReservationFailedEvent.getOrderNumber().toString())).orElse(null);
        if (order != null) {
            order.setStatus("FAILED");
            orderRepository.save(order);
            log.info("Order status updated to FAILED for order {}", order.getId());
        }
    }
}
