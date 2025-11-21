package com.programming.tikam.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    private final String[] freeResourceUrls = {"/swagger-ui.html","swagger-ui/**","v3/api-doc/**",
    "/swagger-resources/**", "/api-docs/**", "/aggregate/**","/actuator/prometheus"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
       return httpSecurity.cors(Customizer.withDefaults()) // enable CORS
                .csrf(csrf -> csrf.disable()).   // disable CSRF for APIs
               authorizeHttpRequests(authorize -> authorize
                       .requestMatchers(freeResourceUrls)
                       .permitAll()
                       .anyRequest()
                .authenticated())
                .oauth2ResourceServer(oath2 ->oath2.jwt(Customizer.withDefaults()))
                .build();
    }
}
