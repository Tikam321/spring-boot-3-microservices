package com.tikam.microservices.order_service.dto.request;

import java.math.BigDecimal;

public record OrderRecord(String orderName, String skuCode, BigDecimal price, Integer quantity) {
}
