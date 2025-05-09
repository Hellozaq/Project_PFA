package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Order;
import com.example.springnodebackend.payload.response.OrderHistoryResponse;
import com.example.springnodebackend.payload.response.OrderSummaryResponse;
import com.example.springnodebackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@Slf4j
public class UserOrderHistoryController {

    private final OrderService orderService;

    /**
     * Get order history for the authenticated user
     * @return List of user's orders with summary information
     */
    @GetMapping("/history")
    public ResponseEntity<OrderHistoryResponse> getOrderHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Fetching order history for user: {}", username);
        
        List<Order> orders = orderService.getUserOrders(username);
        
        // Map orders to summary responses
        List<OrderSummaryResponse> orderSummaries = orders.stream()
                .map(order -> OrderSummaryResponse.builder()
                        .id(order.getId())
                        .status(order.getStatus().toString())
                        .totalAmount(order.getTotalAmount())
                        .itemCount(order.getItems().size())
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        OrderHistoryResponse response = OrderHistoryResponse.builder()
                .orders(orderSummaries)
                .totalOrders(orderSummaries.size())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get detailed information about a specific order
     * @param orderId The ID of the order to retrieve
     * @return The order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Fetching order details for order ID: {} requested by user: {}", orderId, username);
        
        Order order = orderService.getOrderById(orderId);
        
        // Verify that the order belongs to the authenticated user
        if (!order.getUser().getUsername().equals(username)) {
            log.warn("User {} attempted to access order {} belonging to another user", username, orderId);
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(order);
    }
}
