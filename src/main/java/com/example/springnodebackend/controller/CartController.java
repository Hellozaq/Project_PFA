package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Cart;
import com.example.springnodebackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userDetails.getUsername()));
    }
    
    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), productId, quantity));
    }
    
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(userDetails.getUsername(), productId, quantity));
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}