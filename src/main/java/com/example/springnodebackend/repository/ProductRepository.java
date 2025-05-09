package com.example.springnodebackend.repository;

import com.example.springnodebackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Original methods
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByActiveTrue();
    
    // New methods with pagination
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
    
    // Advanced search methods
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    // Methods for popular and featured products
    List<Product> findByActiveTrueAndStockQuantityGreaterThan(int minStock);
    
    // Method to find products by category name
    @Query("SELECT p FROM Product p JOIN p.category c WHERE LOWER(c.name) = LOWER(?1) AND p.active = true")
    List<Product> findByCategoryNameIgnoreCaseAndActiveTrue(String categoryName);
}