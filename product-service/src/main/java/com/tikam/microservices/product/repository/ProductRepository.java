package com.tikam.microservices.product.repository;

import com.tikam.microservices.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseStatus;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

}
