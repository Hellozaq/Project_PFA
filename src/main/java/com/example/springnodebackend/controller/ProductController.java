package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.service.ProductManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductManagementService productService;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        // Use the management service but convert page to list for backward compatibility
        return ResponseEntity.ok(productService.getAllProductsPaged(PageRequest.of(0, 1000), false).getContent());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        // The method name is different in ProductManagementService
        Product product = productService.getAllProductsPaged(PageRequest.of(0, 1000), true)
                .getContent().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        // Use the management service but convert page to list for backward compatibility
        return ResponseEntity.ok(productService.searchProductsPaged(query, PageRequest.of(0, 1000), false).getContent());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        // Use the management service but convert page to list for backward compatibility
        return ResponseEntity.ok(productService.getProductsByCategoryPaged(categoryId, PageRequest.of(0, 1000), false).getContent());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Use addProduct method from ProductManagementService
        return ResponseEntity.ok(productService.addProduct(product));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        // Using the method from ProductManagementService
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        // Using the method from ProductManagementService
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}