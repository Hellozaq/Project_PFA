package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }
    
    /**
     * Advanced search with filtering and sorting
     * 
     * @param query Search query for product name or description
     * @param category Optional category filter
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param sortBy Field to sort by (name, price, createdAt)
     * @param sortDirection Sort direction (asc, desc)
     * @return Filtered and sorted list of products
     */
    public List<Product> searchProducts(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String sortDirection) {
        log.info("Performing advanced search with query: {}, category: {}, price range: {} - {}", query, category, minPrice, maxPrice);
        
        // Start with basic search by name or description
        List<Product> results = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        
        // Apply category filter if provided
        if (category != null && !category.isEmpty()) {
            results = results.stream()
                    .filter(product -> product.getCategory() != null && 
                             product.getCategory().getName().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }
        
        // Apply price filters if provided
        if (minPrice != null) {
            results = results.stream()
                    .filter(product -> product.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        
        if (maxPrice != null) {
            results = results.stream()
                    .filter(product -> product.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        
        // Apply sorting
        boolean isAscending = "asc".equalsIgnoreCase(sortDirection);
        
        switch (sortBy.toLowerCase()) {
            case "price":
                results = isAscending ? 
                        results.stream().sorted(Comparator.comparing(Product::getPrice)).collect(Collectors.toList()) :
                        results.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).collect(Collectors.toList());
                break;
            case "createdat":
                results = isAscending ? 
                        results.stream().sorted(Comparator.comparing(Product::getCreatedAt)).collect(Collectors.toList()) :
                        results.stream().sorted(Comparator.comparing(Product::getCreatedAt).reversed()).collect(Collectors.toList());
                break;
            case "name":
            default:
                results = isAscending ? 
                        results.stream().sorted(Comparator.comparing(Product::getName)).collect(Collectors.toList()) :
                        results.stream().sorted(Comparator.comparing(Product::getName).reversed()).collect(Collectors.toList());
        }
        
        log.info("Search returned {} results", results.size());
        return results;
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product updateProduct(String id, Product product) {
        Product existingProduct = getProductById(id);
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setActive(product.isActive());
        
        return productRepository.save(existingProduct);
    }
    
    @Transactional
    public void deleteProduct(String id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }
    
    @Transactional
    public void updateStock(String productId, int quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() - quantity;
        
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock");
        }
        
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }
    
    /**
     * Get popular products based on sales volume or featured status
     * @return List of popular products
     */
    public List<Product> getPopularProducts() {
        // In a real implementation, this would query sales data
        // For now, just return active products with stock > 0, limited to 10
        return productRepository.findByActiveTrueAndStockQuantityGreaterThan(0)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Get featured products for homepage display
     * @return List of featured products
     */
    public List<Product> getFeaturedProducts() {
        // In a real implementation, this would query products marked as featured
        // For now, just return active products with stock > 0, limited to 6
        return productRepository.findByActiveTrueAndStockQuantityGreaterThan(0)
                .stream()
                .limit(6)
                .collect(Collectors.toList());
    }
}