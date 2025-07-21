package com.ecommerce.controller;

import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.model.Cart;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(@RequestBody CartItemDto cartItemDto) {
        return ResponseEntity.ok(cartService.addItemToCart(cartItemDto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable Long itemId,
            @RequestBody CartItemDto cartItemDto
    ) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
