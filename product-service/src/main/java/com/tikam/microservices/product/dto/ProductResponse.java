package com.tikam.microservices.product.dto;

import java.math.BigDecimal;

public record ProductResponse(String productId, String name, String desc, BigDecimal price, String skuCode) {
}
