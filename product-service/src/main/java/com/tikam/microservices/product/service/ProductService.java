package com.tikam.microservices.product.service;

import com.tikam.microservices.product.dto.ProductRequest;
import com.tikam.microservices.product.dto.ProductResponse;
import com.tikam.microservices.product.model.Product;
import com.tikam.microservices.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = new Product(productRequest.name(), productRequest.description(), productRequest.price(), productRequest.skuCode());
        productRepository.save(product);
        log.info("Product is created");
        return new ProductResponse(product.getId(), product.getName(),product.getDescription(),product.getPrice(),product.getSkeCode());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(product -> new ProductResponse(product.getId(), product.getName(),product.getDescription(),product.getPrice(), product.getSkeCode())).collect(Collectors.toList());
    }
}
