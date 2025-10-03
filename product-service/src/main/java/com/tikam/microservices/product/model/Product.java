package com.tikam.microservices.product.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Document(value = "product")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Product {
    @Id
    private String id;
    private String skeCode;
    private String name;
    private String description;
    private BigDecimal price;
//    public Product() {
////    }
//

    public Product(String name, String description, BigDecimal price, String skuCode) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.skeCode = skuCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSkeCode() {
        return skeCode;
    }

    public void setSkeCode(String skeCode) {
        this.skeCode = skeCode;
    }
}
