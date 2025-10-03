package com.tikam.microservices.notification_service.events.order;

public class Order {
        private String orderNumber;
        private String email;

    public Order(String orderId, String email) {
        this.orderNumber = orderId;
        this.email = email;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
