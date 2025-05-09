package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.*;
import com.example.springnodebackend.payload.request.CheckoutRequest;
import com.example.springnodebackend.service.CartService;
import com.example.springnodebackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest checkoutRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Processing checkout for user: {}", username);

            // Get the user's cart
            Cart cart = cartService.getOrCreateCart(username);
            
            if (cart.getItems().isEmpty()) {
                log.warn("Attempted checkout with empty cart for user: {}", username);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cart is empty",
                    "message", "Please add items to your cart before checkout"
                ));
            }

            // Create a new order
            Order order = new Order();
            
            // Set the user
            User user = new User();
            user.setId(cart.getUser().getId());
            user.setUsername(username);
            order.setUser(user);
            
            // Set shipping address
            Address shippingAddress = new Address();
            shippingAddress.setStreet(checkoutRequest.getStreet());
            shippingAddress.setCity(checkoutRequest.getCity());
            shippingAddress.setCountry(checkoutRequest.getCountry());
            shippingAddress.setPhone(checkoutRequest.getPhone());
            order.setShippingAddress(shippingAddress);
            
            // Set payment ID if provided
            if (checkoutRequest.getPaymentId() != null) {
                order.setPaymentId(checkoutRequest.getPaymentId());
            }
            
            // Convert cart items to order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItems.add(orderItem);
            }
            order.setItems(orderItems);
            
            // Calculate total (will be recalculated in OrderService)
            BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
            
            // Create the order
            Order createdOrder = orderService.createOrder(order);
            
            // Clear the cart after successful order creation
            cartService.clearCart(username);
            
            log.info("Checkout completed successfully for user: {}. Order ID: {}", username, createdOrder.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order created successfully",
                "orderId", createdOrder.getId(),
                "orderStatus", createdOrder.getStatus(),
                "totalAmount", createdOrder.getTotalAmount()
            ));
            
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Checkout failed",
                "message", e.getMessage()
            ));
        }
    }
}
