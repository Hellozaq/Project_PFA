package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Cart;
import com.example.springnodebackend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<Cart> getCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Getting cart for user: {}", username);
        return ResponseEntity.ok(cartService.getOrCreateCart(username));
    }
    
    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(
            @RequestParam String productId,
            @RequestParam int quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Adding product {} to cart for user: {}", productId, username);
        return ResponseEntity.ok(cartService.addToCart(username, productId, quantity));
    }
    
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable String productId,
            @RequestParam int quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Updating product {} quantity to {} for user: {}", productId, quantity, username);
        return ResponseEntity.ok(cartService.updateCartItemQuantity(username, productId, quantity));
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("Clearing cart for user: {}", username);
        cartService.clearCart(username);
        return ResponseEntity.ok().build();
    }
}