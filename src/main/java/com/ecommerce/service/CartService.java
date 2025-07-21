package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartDto getCart() {
        Cart cart = getCurrentUserCart();
        return mapToDto(cart);
    }

    public CartDto addItemToCart(CartItemDto cartItemDto) {
        Cart cart = getCurrentUserCart();
        Product product = productRepository.findById(cartItemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartItemDto.getProductId()));
        
        // Check if the product is already in the cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity if product already exists in cart
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDto.getQuantity());
        } else {
            // Add new item to cart
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDto.getQuantity());
            cart.addItem(cartItem);
        }
        
        cartRepository.save(cart);
        return mapToDto(cart);
    }

    public CartDto updateCartItem(Long itemId, CartItemDto cartItemDto) {
        Cart cart = getCurrentUserCart();
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
        
        if (cartItemDto.getQuantity() <= 0) {
            cart.removeItem(cartItem);
        } else {
            cartItem.setQuantity(cartItemDto.getQuantity());
        }
        
        cartRepository.save(cart);
        return mapToDto(cart);
    }

    public void removeItemFromCart(Long itemId) {
        Cart cart = getCurrentUserCart();
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
        
        cart.removeItem(cartItem);
        cartRepository.save(cart);
    }

    public void clearCart() {
        Cart cart = getCurrentUserCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public void createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
    }

    private Cart getCurrentUserCart() {
        User user = userService.getCurrentUser();
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getUsername()));
    }

    private CartDto mapToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        
        cartDto.setItems(cart.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList()));
        
        // Calculate total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cartDto.setTotalPrice(totalPrice);
        
        return cartDto;
    }

    private CartItemDto mapItemToDto(CartItem cartItem) {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setId(cartItem.getId());
        cartItemDto.setProductId(cartItem.getProduct().getId());
        cartItemDto.setProductName(cartItem.getProduct().getName());
        cartItemDto.setPrice(cartItem.getProduct().getPrice());
        cartItemDto.setQuantity(cartItem.getQuantity());
        cartItemDto.setSubTotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());
        return cartItemDto;
    }
}
