package com.ecommerce.platform.product.config;

import com.ecommerce.platform.product.domain.Product;
import com.ecommerce.platform.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(new Product("SKU-1001", "Laptop Pro 14", "Enterprise ultrabook", new BigDecimal("1899.00"), true));
            repository.save(new Product("SKU-1002", "Noise Cancelling Headset", "Support desk headset", new BigDecimal("249.00"), true));
            repository.save(new Product("SKU-1003", "Docking Station", "USB-C dock", new BigDecimal("179.00"), true));
        };
    }
}
