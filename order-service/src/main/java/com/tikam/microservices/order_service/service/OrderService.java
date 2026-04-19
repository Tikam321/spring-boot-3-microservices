package com.tikam.microservices.order_service.service;

import com.tikam.microservices.order_service.dto.request.OrderRequest;
import com.tikam.microservices.order_service.entity.Order;
import com.tikam.microservices.order_service.event.OrderPlacedEvent;
import com.tikam.microservices.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order placeOrder(OrderRequest orderRequest) {
        // 1. Create Order in PENDING state
        Order order = new Order(orderRequest.getOrderName(), orderRequest.getSkuCode(),
                orderRequest.getPrice(), orderRequest.getQuantity());
        order.setStatus("PENDING");
        order.setEmail(orderRequest.getEmail());
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with status PENDING: {}", savedOrder.getId());

        // 2. Publish OrderPlacedEvent to Kafka
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderNumber(savedOrder.getId().toString());
        orderPlacedEvent.setSkuCode(orderRequest.getSkuCode());
        orderPlacedEvent.setEmail(orderRequest.getEmail());
        orderPlacedEvent.setFirstName("tikam");
        orderPlacedEvent.setLastName("suvasiya");

        log.info("Sending OrderPlacedEvent to topic order-placed");
        kafkaTemplate.send("order-placed", orderPlacedEvent);
        
        return savedOrder;
    }
}
