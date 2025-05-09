package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Cart;
import com.example.springnodebackend.model.CartItem;
import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.model.User;
import com.example.springnodebackend.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductService productService;
    
    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create a new cart with a properly initialized user
                    User user = new User();
                    user.setId(userId);
                    
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }
    
    @Transactional
    public Cart addToCart(String userId, String productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.getProductById(productId);
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });
        
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart updateCartItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));
        
        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
        } else {
            Product product = productService.getProductById(productId);
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            cartItem.setQuantity(quantity);
        }
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}