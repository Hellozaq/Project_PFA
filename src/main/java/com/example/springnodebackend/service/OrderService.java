package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Order;
import com.example.springnodebackend.model.OrderItem;
import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.payload.request.NotificationRequest;
import com.example.springnodebackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final NodeServiceClient nodeServiceClient;
    
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    @Transactional
    public Order createOrder(Order order) {
        // Set bidirectional relationship for OrderItems
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
        }
        
        validateAndUpdateStock(order.getItems());
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String now = LocalDateTime.now().format(formatter);
        
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setTotalAmount(calculateTotal(order.getItems()));
        
        Order savedOrder = orderRepository.save(order);
        
        try {
            // Send notification
            nodeServiceClient.sendNotification(
                NotificationRequest.builder()
                    .recipient(order.getUser().getId())
                    .type("ORDER_CREATED")
                    .content("Your order has been created successfully")
                    .data(savedOrder)
                    .build(),
                null
            );
        } catch (Exception e) {
            // Log the error but don't fail the order creation
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return savedOrder;
    }
    
    @Transactional
    public Order updateOrderStatus(String id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        Order updatedOrder = orderRepository.save(order);
        
        try {
            // Send notification
            nodeServiceClient.sendNotification(
                NotificationRequest.builder()
                    .recipient(order.getUser().getId())
                    .type("ORDER_STATUS_UPDATED")
                    .content("Your order status has been updated to " + status)
                    .data(updatedOrder)
                    .build(),
                null
            );
        } catch (Exception e) {
            // Log the error but don't fail the order status update
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return updatedOrder;
    }
    
    private void validateAndUpdateStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new IllegalArgumentException("Product information is missing in order item");
            }
            
            Product product = productService.getProductById(item.getProduct().getId());
            
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity() + ", Requested: " + item.getQuantity());
            }
            
            // Set the price from the current product price if not already set
            if (item.getPrice() == null) {
                item.setPrice(product.getPrice());
            }
            
            productService.updateStock(product.getId(), item.getQuantity());
        }
    }
    
    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}