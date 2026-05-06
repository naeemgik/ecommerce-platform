package com.ecommerce.platform.cart.web;

import com.ecommerce.platform.cart.service.CartService;
import com.ecommerce.platform.cart.web.dto.AddCartItemRequest;
import com.ecommerce.platform.cart.web.dto.CartItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carts/{customerId}/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItemResponse> getCart(@PathVariable Long customerId) {
        return cartService.getCart(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(@PathVariable Long customerId, @Valid @RequestBody AddCartItemRequest request) {
        cartService.addItem(customerId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
    }
}
