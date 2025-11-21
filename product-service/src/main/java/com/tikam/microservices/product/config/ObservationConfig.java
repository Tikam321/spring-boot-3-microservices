package com.tikam.microservices.product.config;

import com.mongodb.event.CommandListener;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;

//@Configuration
//public class ObservationConfig {
//    @Bean
//    ObservedAspect observedAspect(ObservationRegistry registry) {
//        return new ObservedAspect(registry);
//
//    }
//}

@Configuration
public class ObservationConfig {
    @Bean
    ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    @Bean
    public CommandListener mongoObservationCommandListener(ObservationRegistry registry) {
        return new MongoObservationCommandListener(registry);
    }
}