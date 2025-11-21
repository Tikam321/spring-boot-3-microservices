package com.tikam.microservices.order_service.service;


import com.tikam.microservices.order_service.client.InventoryClient;
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
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient,
                        KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order placeOrder(OrderRequest orderRequest) {
        // 1. mockito mock(to mock this method)
        // 2. wiremock (to mock the api)(external api communication)
        boolean inStock = inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
        if (inStock) {

            System.out.println("inside the  in stock condition");
            Order order = new Order(orderRequest.getOrderName(),orderRequest.getSkuCode(),
                    orderRequest.getPrice(),orderRequest.getQuantity());

            // reduce the count in the skuCode product in inventory
            try {
                boolean orderPlaced = inventoryClient.placeOrder(orderRequest.getSkuCode());
                System.out.println("the order places status is " + orderPlaced);
                log.info("the order places api is called in rest client inventory microservices");
            } catch (Exception e) {
                System.out.println("there is some error is coming inventory operations " + e.getMessage());
            }

            Order savedOrder = orderRepository.save(order);
            System.out.println("order is saved");
            // send the messages to kafka
            //email and orderId
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(Long.toString(1231L));
            orderPlacedEvent.setEmail("test@gmail.com");
            orderPlacedEvent.setFirstName("tikam");
            orderPlacedEvent.setLastName("suvasiya");

            log.info("sending the message for order  to topic order-placed");
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("the order message is published on topic order-placed");
            return savedOrder;
        } else {
            throw new RuntimeException("Product with skuCode n" + orderRequest.getSkuCode() + " is out of stock");
        }
    }
}
