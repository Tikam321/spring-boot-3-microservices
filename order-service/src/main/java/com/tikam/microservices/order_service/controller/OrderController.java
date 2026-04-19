package com.tikam.microservices.order_service.controller;

import com.tikam.microservices.order_service.dto.request.OrderRequest;
import com.tikam.microservices.order_service.entity.Order;
import com.tikam.microservices.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private Logger log = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
//    @CircuitBreaker(
//            name = "inventory",
//            fallbackMethod = "fallbackMethod"
//    )
//    @Retry(name = "inventory")
    ResponseEntity<Order> createOrder(@NonNull @RequestBody OrderRequest orderRecord) {
        return ResponseEntity.status(201).body(orderService.placeOrder(orderRecord));
    }

    public  ResponseEntity<Order> fallbackMethod(OrderRequest orderRequest, Throwable t) {
        log.info("cannot ge skuCode {},failure reason {},", orderRequest.getSkuCode(), t.getMessage());
        Order fallbackorder = new Order(orderRequest.getOrderName(),orderRequest.getSkuCode(),orderRequest.getPrice(),orderRequest.getQuantity());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackorder);
    }
}
