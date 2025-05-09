package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Cart;
import com.example.springnodebackend.model.User;
import com.example.springnodebackend.repository.CartRepository;
import com.example.springnodebackend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart/debug")
@RequiredArgsConstructor
@Slf4j
public class CartDebugController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    @GetMapping("/test")
    public ResponseEntity<?> testCartAccess() {
        Map<String, Object> response = new HashMap<>();
        
        // Get authentication info
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("isAuthenticated", true);
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            
            try {
                // Try to get the cart
                Cart cart = cartService.getOrCreateCart(authentication.getName());
                response.put("cartFound", true);
                response.put("cartId", cart.getId());
                response.put("cartItemsCount", cart.getItems().size());
            } catch (Exception e) {
                response.put("cartFound", false);
                response.put("cartError", e.getMessage());
                log.error("Error getting cart: {}", e.getMessage(), e);
            }
        } else {
            response.put("isAuthenticated", false);
            response.put("message", "User is not authenticated");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Try to get the cart directly by user ID
            Cart cart = cartService.getOrCreateCart(userId);
            response.put("cartFound", true);
            response.put("cartId", cart.getId());
            response.put("cartItemsCount", cart.getItems().size());
        } catch (Exception e) {
            response.put("cartFound", false);
            response.put("cartError", e.getMessage());
            log.error("Error getting cart for user {}: {}", userId, e.getMessage(), e);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/force-create/{userId}")
    public ResponseEntity<?> forceCreateCart(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create a user object
            User user = new User();
            user.setId(userId);
            
            // Create a new cart
            Cart cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
            
            response.put("success", true);
            response.put("cartId", cart.getId());
            response.put("userId", userId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Error creating cart for user {}: {}", userId, e.getMessage(), e);
        }
        
        return ResponseEntity.ok(response);
    }
}
