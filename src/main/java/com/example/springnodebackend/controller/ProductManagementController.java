package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.service.ProductManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for sophisticated product management
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductManagementController {

    private final ProductManagementService productManagementService;
    
    /**
     * Add a new product
     */
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productManagementService.addProduct(product));
    }
    
    /**
     * Add multiple products at once
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> addMultipleProducts(@RequestBody List<Product> products) {
        return ResponseEntity.ok(productManagementService.addMultipleProducts(products));
    }
    
    /**
     * Update an existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return ResponseEntity.ok(productManagementService.updateProduct(id, product));
    }
    
    /**
     * Delete a product permanently
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productManagementService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Make a product unavailable (soft delete)
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Product> deactivateProduct(@PathVariable String id) {
        return ResponseEntity.ok(productManagementService.deactivateProduct(id));
    }
    
    /**
     * Make a product available again
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Product> activateProduct(@PathVariable String id) {
        return ResponseEntity.ok(productManagementService.activateProduct(id));
    }
    
    /**
     * Update product stock quantity
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable String id, 
            @RequestBody Map<String, Integer> payload) {
        Integer newStock = payload.get("stockQuantity");
        if (newStock == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(productManagementService.updateStock(id, newStock));
    }
    
    /**
     * Update product price
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable String id, 
            @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal newPrice = payload.get("price");
        if (newPrice == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(productManagementService.updatePrice(id, newPrice));
    }
    
    /**
     * Bulk update product availability
     */
    @PutMapping("/bulk/availability")
    public ResponseEntity<Void> bulkUpdateAvailability(
            @RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> productIds = (List<String>) payload.get("productIds");
        Boolean active = (Boolean) payload.get("active");
        
        if (productIds == null || active == null) {
            return ResponseEntity.badRequest().build();
        }
        
        productManagementService.bulkUpdateAvailability(productIds, active);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get all products with pagination
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProductsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(productManagementService.getAllProductsPaged(pageRequest, includeInactive));
    }
    
    /**
     * Get products by category with pagination
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategoryPaged(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(productManagementService.getProductsByCategoryPaged(categoryId, pageRequest, includeInactive));
    }
    
    /**
     * Search products with pagination
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProductsPaged(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(productManagementService.searchProductsPaged(query, pageRequest, includeInactive));
    }
}
