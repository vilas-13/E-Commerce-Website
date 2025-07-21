package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserService userService;

    public OrderDto createOrder() {
        Cart cart = cartService.getCurrentUserCart();
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }
        
        // Check stock availability
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStockQuantity() + 
                        ", Requested: " + cartItem.getQuantity()
                );
            }
        }
        
        // Create order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PROCESSING");
        
        // Convert cart items to order items and update product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            
            order.addItem(orderItem);
            
            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // Save order and clear cart
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();
        
        return mapToDto(savedOrder);
    }

    public List<OrderDto> getUserOrders() {
        User user = userService.getCurrentUser();
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        // Verify that the order belongs to the current user
        User currentUser = userService.getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        
        return mapToDto(order);
    }

    private OrderDto mapToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setStatus(order.getStatus());
        
        orderDto.setItems(order.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList()));
        
        // Calculate total price
        double totalPrice = order.getItems().stream()
                .mapToDouble(item -> item.getPriceAtPurchase() * item.getQuantity())
                .sum();
        orderDto.setTotalPrice(totalPrice);
        
        return orderDto;
    }

    private OrderItemDto mapItemToDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setId(orderItem.getId());
        orderItemDto.setProductId(orderItem.getProduct().getId());
        orderItemDto.setProductName(orderItem.getProduct().getName());
        orderItemDto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setSubTotal(orderItem.getPriceAtPurchase() * orderItem.getQuantity());
        return orderItemDto;
    }
}