package com.ecommerce.platform.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient productServiceWebClient(WebClient.Builder builder, @Value("${clients.product-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
