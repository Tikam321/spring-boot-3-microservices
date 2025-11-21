package com.tikam.microservices.order_service.config;


import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ObservationConfig {

        private final KafkaTemplate<String, Object> kafkaTemplate;

    public ObservationConfig(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
        public void setObservationForKafkaTemplate() {
            kafkaTemplate.setObservationEnabled(true);
        }

        @Bean
        ObservedAspect observedAspect(ObservationRegistry registry) {
            return new ObservedAspect(registry);
        }
}

