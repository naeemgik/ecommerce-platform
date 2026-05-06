package com.ecommerce.platform.product.web;

import com.ecommerce.platform.events.ProductView;
import com.ecommerce.platform.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}")
    public CompletableFuture<ResponseEntity<ProductView>> getProduct(@PathVariable Long productId) {
        return productService.getProductAsync(productId)
                .thenApply(ResponseEntity::ok);
    }
}
