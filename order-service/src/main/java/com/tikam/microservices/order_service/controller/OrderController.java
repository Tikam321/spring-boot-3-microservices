package com.tikam.microservices.order_service.controller;

import com.tikam.microservices.order_service.dto.request.OrderRequest;
import com.tikam.microservices.order_service.entity.Order;
import com.tikam.microservices.order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping
    ResponseEntity<Order> createOrder(@NonNull @RequestBody OrderRequest orderRecord) {
        return ResponseEntity.status(201).body(orderService.placeOrder(orderRecord));
    }
}
