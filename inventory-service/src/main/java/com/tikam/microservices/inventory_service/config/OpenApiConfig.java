package com.tikam.microservices.inventory_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI productServiceApi() {
        return new OpenAPI()
                .info(new Info().title("Product Service API")
                        .description("this is the REST API for prodduct service")
                        .license(new License().name("Apache 22.0")))
                .externalDocs(new ExternalDocumentation().description("You can refer to the product service")
                        .url("https://product-service/docs"));

    }
}
