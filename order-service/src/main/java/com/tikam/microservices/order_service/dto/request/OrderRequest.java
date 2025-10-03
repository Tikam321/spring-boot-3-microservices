package com.tikam.microservices.order_service.dto.request;

import java.math.BigDecimal;

public class OrderRequest {
    private String orderName;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private String email;

    public OrderRequest(String orderName, String email, Integer quantity, BigDecimal price, String skuCod) {
        this.orderName = orderName;
        this.email = email;
        this.quantity = quantity;
        this.price = price;
        this.skuCode = skuCod;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
