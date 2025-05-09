package com.example.springnodebackend.controller;

import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.payload.response.ProductSearchResponse;
import com.example.springnodebackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products/search")
@RequiredArgsConstructor
@Slf4j
public class ProductSearchController {

    private final ProductService productService;

    /**
     * Search for products by name, description, or category
     * @param query The search query
     * @param category Optional category filter
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param sortBy Optional sort field (name, price, createdAt)
     * @param sortDirection Optional sort direction (asc, desc)
     * @return List of matching products
     */
    @GetMapping
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        
        log.info("Searching products with query: {}, category: {}, price range: {} - {}, sort: {} {}", 
                query, category, minPrice, maxPrice, sortBy, sortDirection);
        
        List<Product> products = productService.searchProducts(query, category, minPrice, maxPrice, sortBy, sortDirection);
        
        ProductSearchResponse response = ProductSearchResponse.builder()
                .products(products)
                .count(products.size())
                .query(query)
                .filters(ProductSearchResponse.SearchFilters.builder()
                        .category(category)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product recommendations based on user browsing history or popular items
     * @return List of recommended products
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<Product>> getRecommendedProducts() {
        log.info("Fetching product recommendations");
        
        // For now, just return popular products
        // In a real implementation, this would use user history and preferences
        List<Product> recommendations = productService.getPopularProducts();
        
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Get featured products for homepage display
     * @return List of featured products
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        log.info("Fetching featured products");
        
        List<Product> featuredProducts = productService.getFeaturedProducts();
        
        return ResponseEntity.ok(featuredProducts);
    }
}
