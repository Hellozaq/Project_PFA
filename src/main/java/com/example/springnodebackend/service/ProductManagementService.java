package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Category;
import com.example.springnodebackend.model.Product;
import com.example.springnodebackend.repository.CategoryRepository;
import com.example.springnodebackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@DependsOn("categoryService") // Ensure categories are initialized first
public class ProductManagementService implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Add a new product with detailed information
     */
    @Transactional
    public Product addProduct(Product product) {
        validateProduct(product);
        
        // Set timestamps
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        // Ensure product is active by default
        product.setActive(true);
        
        log.info("Adding new product: {}", product.getName());
        return productRepository.save(product);
    }
    
    /**
     * Add multiple products at once
     */
    @Transactional
    public List<Product> addMultipleProducts(List<Product> products) {
        products.forEach(this::validateProduct);
        
        // Set timestamps and active status for all products
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        products.forEach(product -> {
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            product.setActive(true);
        });
        
        log.info("Adding {} new products", products.size());
        return productRepository.saveAll(products);
    }
    
    /**
     * Update an existing product
     */
    @Transactional
    public Product updateProduct(String id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        
        // Update timestamp
        existingProduct.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        log.info("Updating product: {}", existingProduct.getName());
        return productRepository.save(existingProduct);
    }
    
    /**
     * Delete a product permanently
     */
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        log.info("Permanently deleting product: {}", product.getName());
        productRepository.delete(product);
    }
    
    /**
     * Make a product unavailable (soft delete)
     */
    @Transactional
    public Product deactivateProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        log.info("Deactivating product: {}", product.getName());
        return productRepository.save(product);
    }
    
    /**
     * Make a product available again
     */
    @Transactional
    public Product activateProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        log.info("Activating product: {}", product.getName());
        return productRepository.save(product);
    }
    
    /**
     * Update product stock quantity
     */
    @Transactional
    public Product updateStock(String id, Integer newStockQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        product.setStockQuantity(newStockQuantity);
        product.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        log.info("Updating stock for product {}: new quantity = {}", product.getName(), newStockQuantity);
        return productRepository.save(product);
    }
    
    /**
     * Update product price
     */
    @Transactional
    public Product updatePrice(String id, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        
        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        log.info("Updating price for product {}: new price = {}", product.getName(), newPrice);
        return productRepository.save(product);
    }
    
    /**
     * Bulk update product availability
     */
    @Transactional
    public void bulkUpdateAvailability(List<String> productIds, boolean active) {
        for (String id : productIds) {
            Optional<Product> productOpt = productRepository.findById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setActive(active);
                product.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                productRepository.save(product);
                log.info("Bulk update: Setting product {} to active={}", product.getName(), active);
            } else {
                log.warn("Product with ID {} not found during bulk update", id);
            }
        }
    }
    
    /**
     * Get all products with pagination
     */
    public Page<Product> getAllProductsPaged(Pageable pageable, boolean includeInactive) {
        if (includeInactive) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findByActiveTrue(pageable);
        }
    }
    
    /**
     * Get products by category with pagination
     */
    public Page<Product> getProductsByCategoryPaged(Long categoryId, Pageable pageable, boolean includeInactive) {
        if (includeInactive) {
            return productRepository.findByCategoryId(categoryId, pageable);
        } else {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        }
    }
    
    /**
     * Search products with pagination
     */
    public Page<Product> searchProductsPaged(String query, Pageable pageable, boolean includeInactive) {
        if (includeInactive) {
            return productRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query, pageable);
        }
    }
    
    /**
     * Create a product builder with default values
     */
    public Product.ProductBuilder createProductBuilder(String name, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        
        return Product.builder()
                .name(name)
                .category(category)
                .active(true)
                .stockQuantity(0)
                .price(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }
    
    /**
     * Validate product data
     */
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Product stock quantity cannot be negative");
        }
        
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Product must have a category");
        }
        
        // Verify category exists
        Long categoryId = product.getCategory().getId();
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category with ID " + categoryId + " does not exist");
        }
    }
    
    /**
     * Initialize products when the application starts
     */
    @Override
    @Transactional
    public void run(String... args) {
        // Only add products if none exist
        if (productRepository.count() > 0) {
            log.info("Products already exist, skipping initialization");
            return;
        }

        log.info("Initializing jewelry products...");
        
        // Get all categories
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("No categories found, cannot initialize products");
            return;
        }
        
        // Find each category by name
        Category baguesCategory = findCategoryByName(categories, "Bagues");
        Category collierCategory = findCategoryByName(categories, "Collier");
        Category braceletsCategory = findCategoryByName(categories, "Bracelets");
        Category bouclesCategory = findCategoryByName(categories, "Boucle d'oreilles");
        Category chainesCategory = findCategoryByName(categories, "Chaine de cheville");
        Category montresCategory = findCategoryByName(categories, "Montres");
        
        List<Product> allProducts = new ArrayList<>();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        
        // Add rings (10 products)
        if (baguesCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Bague Diamant Solitaire", "Élégante bague avec diamant solitaire de 0.5 carat", new BigDecimal("599.99"), 10, baguesCategory, "bague-diamant-solitaire.jpg", now),
                createProduct("Alliance Or Blanc", "Alliance classique en or blanc 18 carats", new BigDecimal("349.99"), 15, baguesCategory, "alliance-or-blanc.jpg", now),
                createProduct("Bague Trio de Pierres", "Bague avec trois pierres précieuses alignées", new BigDecimal("449.99"), 8, baguesCategory, "bague-trio-pierres.jpg", now),
                createProduct("Chevalière Homme", "Chevalière élégante pour homme en argent massif", new BigDecimal("199.99"), 12, baguesCategory, "chevaliere-homme.jpg", now),
                createProduct("Bague Fleur", "Bague en forme de fleur avec pierres colorées", new BigDecimal("279.99"), 10, baguesCategory, "bague-fleur.jpg", now),
                createProduct("Bague Tressée", "Bague au design tressé en or rose", new BigDecimal("329.99"), 7, baguesCategory, "bague-tressee.jpg", now),
                createProduct("Bague Émeraude", "Somptueuse bague avec émeraude centrale", new BigDecimal("699.99"), 5, baguesCategory, "bague-emeraude.jpg", now),
                createProduct("Bague Infini", "Bague symbole infini en argent et zirconium", new BigDecimal("159.99"), 20, baguesCategory, "bague-infini.jpg", now),
                createProduct("Bague Vintage", "Bague style vintage avec motifs ciselés", new BigDecimal("249.99"), 8, baguesCategory, "bague-vintage.jpg", now),
                createProduct("Bague Perle", "Élégante bague avec perle de culture", new BigDecimal("299.99"), 10, baguesCategory, "bague-perle.jpg", now)
            ));
        }
        
        // Add necklaces (8 products)
        if (collierCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Collier Pendentif Cœur", "Collier avec pendentif en forme de cœur en or rose", new BigDecimal("249.99"), 12, collierCategory, "collier-coeur.jpg", now),
                createProduct("Collier Perles", "Collier de perles de culture blanches", new BigDecimal("399.99"), 8, collierCategory, "collier-perles.jpg", now),
                createProduct("Collier Chaîne Or", "Collier chaîne fine en or 18 carats", new BigDecimal("349.99"), 15, collierCategory, "collier-chaine-or.jpg", now),
                createProduct("Collier Croix", "Collier avec pendentif croix en argent", new BigDecimal("179.99"), 10, collierCategory, "collier-croix.jpg", now),
                createProduct("Collier Étoile", "Collier avec pendentif étoile serti de diamants", new BigDecimal("299.99"), 7, collierCategory, "collier-etoile.jpg", now),
                createProduct("Collier Ras de Cou", "Collier ras de cou en argent avec zirconiums", new BigDecimal("159.99"), 20, collierCategory, "collier-ras-de-cou.jpg", now),
                createProduct("Collier Multi-rangs", "Collier élégant à plusieurs rangs", new BigDecimal("229.99"), 10, collierCategory, "collier-multi-rangs.jpg", now),
                createProduct("Collier Prénom", "Collier personnalisable avec prénom en or", new BigDecimal("279.99"), 15, collierCategory, "collier-prenom.jpg", now)
            ));
        }
        
        // Add bracelets (8 products)
        if (braceletsCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Bracelet Tennis", "Bracelet tennis avec diamants en ligne", new BigDecimal("499.99"), 6, braceletsCategory, "bracelet-tennis.jpg", now),
                createProduct("Bracelet Jonc", "Bracelet jonc rigide en or", new BigDecimal("299.99"), 10, braceletsCategory, "bracelet-jonc.jpg", now),
                createProduct("Bracelet Cuir Homme", "Bracelet en cuir tressé pour homme", new BigDecimal("89.99"), 15, braceletsCategory, "bracelet-cuir-homme.jpg", now),
                createProduct("Bracelet Charm", "Bracelet avec charms personnalisables", new BigDecimal("159.99"), 12, braceletsCategory, "bracelet-charm.jpg", now),
                createProduct("Bracelet Perles", "Bracelet en perles naturelles", new BigDecimal("129.99"), 20, braceletsCategory, "bracelet-perles.jpg", now),
                createProduct("Bracelet Chaîne", "Bracelet chaîne fine en argent", new BigDecimal("119.99"), 25, braceletsCategory, "bracelet-chaine.jpg", now),
                createProduct("Bracelet Manchette", "Bracelet manchette en argent ciselé", new BigDecimal("179.99"), 8, braceletsCategory, "bracelet-manchette.jpg", now),
                createProduct("Bracelet Cordon", "Bracelet cordon avec fermoir en or", new BigDecimal("99.99"), 15, braceletsCategory, "bracelet-cordon.jpg", now)
            ));
        }
        
        // Add earrings (8 products)
        if (bouclesCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Boucles d'Oreilles Diamant", "Boucles d'oreilles puces avec diamants", new BigDecimal("399.99"), 10, bouclesCategory, "boucles-diamant.jpg", now),
                createProduct("Créoles Or", "Créoles classiques en or jaune", new BigDecimal("249.99"), 15, bouclesCategory, "creoles-or.jpg", now),
                createProduct("Boucles d'Oreilles Pendantes", "Élégantes boucles d'oreilles pendantes avec cristaux", new BigDecimal("179.99"), 12, bouclesCategory, "boucles-pendantes.jpg", now),
                createProduct("Boucles d'Oreilles Perle", "Boucles d'oreilles avec perles de culture", new BigDecimal("229.99"), 10, bouclesCategory, "boucles-perle.jpg", now),
                createProduct("Boucles d'Oreilles Étoile", "Boucles d'oreilles en forme d'étoile", new BigDecimal("149.99"), 18, bouclesCategory, "boucles-etoile.jpg", now),
                createProduct("Boucles d'Oreilles Fleur", "Boucles d'oreilles en forme de fleur", new BigDecimal("169.99"), 15, bouclesCategory, "boucles-fleur.jpg", now),
                createProduct("Ear Cuffs", "Ear cuffs modernes en argent", new BigDecimal("99.99"), 20, bouclesCategory, "ear-cuffs.jpg", now),
                createProduct("Boucles d'Oreilles Goutte", "Boucles d'oreilles en forme de goutte d'eau", new BigDecimal("189.99"), 10, bouclesCategory, "boucles-goutte.jpg", now)
            ));
        }
        
        // Add ankle chains (8 products)
        if (chainesCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Chaîne de Cheville Simple", "Chaîne de cheville fine en argent", new BigDecimal("79.99"), 20, chainesCategory, "chaine-cheville-simple.jpg", now),
                createProduct("Chaîne de Cheville Perles", "Chaîne de cheville avec petites perles", new BigDecimal("99.99"), 15, chainesCategory, "chaine-cheville-perles.jpg", now),
                createProduct("Chaîne de Cheville Breloques", "Chaîne de cheville avec breloques", new BigDecimal("89.99"), 18, chainesCategory, "chaine-cheville-breloques.jpg", now),
                createProduct("Chaîne de Cheville Or", "Chaîne de cheville en or 18 carats", new BigDecimal("199.99"), 8, chainesCategory, "chaine-cheville-or.jpg", now),
                createProduct("Chaîne de Cheville Étoile", "Chaîne de cheville avec pendentif étoile", new BigDecimal("89.99"), 15, chainesCategory, "chaine-cheville-etoile.jpg", now),
                createProduct("Chaîne de Cheville Cœur", "Chaîne de cheville avec pendentif cœur", new BigDecimal("99.99"), 12, chainesCategory, "chaine-cheville-coeur.jpg", now),
                createProduct("Chaîne de Cheville Double", "Chaîne de cheville double rang", new BigDecimal("119.99"), 10, chainesCategory, "chaine-cheville-double.jpg", now),
                createProduct("Chaîne de Cheville Colorée", "Chaîne de cheville avec pierres colorées", new BigDecimal("109.99"), 12, chainesCategory, "chaine-cheville-coloree.jpg", now)
            ));
        }
        
        // Add watches (8 products)
        if (montresCategory != null) {
            allProducts.addAll(Arrays.asList(
                createProduct("Montre Classique Homme", "Montre élégante pour homme avec bracelet en cuir", new BigDecimal("299.99"), 10, montresCategory, "montre-classique-homme.jpg", now),
                createProduct("Montre Femme Or Rose", "Montre pour femme en or rose avec cadran nacré", new BigDecimal("349.99"), 8, montresCategory, "montre-femme-or-rose.jpg", now),
                createProduct("Montre Chronographe", "Montre chronographe sportive pour homme", new BigDecimal("399.99"), 12, montresCategory, "montre-chronographe.jpg", now),
                createProduct("Montre Bracelet Acier", "Montre avec bracelet en acier inoxydable", new BigDecimal("279.99"), 15, montresCategory, "montre-bracelet-acier.jpg", now),
                createProduct("Montre Automatique", "Montre automatique avec mouvement apparent", new BigDecimal("599.99"), 6, montresCategory, "montre-automatique.jpg", now),
                createProduct("Montre Minimaliste", "Montre au design épuré et minimaliste", new BigDecimal("199.99"), 20, montresCategory, "montre-minimaliste.jpg", now),
                createProduct("Montre Connectée", "Montre connectée avec multiples fonctionnalités", new BigDecimal("249.99"), 15, montresCategory, "montre-connectee.jpg", now),
                createProduct("Montre Vintage", "Montre au style rétro vintage", new BigDecimal("229.99"), 10, montresCategory, "montre-vintage.jpg", now)
            ));
        }
        
        // Save all products
        productRepository.saveAll(allProducts);
        log.info("Added {} jewelry products successfully", allProducts.size());
    }
    
    private Category findCategoryByName(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    private Product createProduct(String name, String description, BigDecimal price, 
                                 Integer stockQuantity, Category category, 
                                 String imageFileName, String timestamp) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .imageUrl("https://example.com/images/" + imageFileName)
                .active(true)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
