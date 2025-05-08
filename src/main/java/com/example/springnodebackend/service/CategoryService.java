package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Category;
import com.example.springnodebackend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService implements CommandLineRunner {
    
    private final CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        
        // Check if the new name already exists and it's not the same category
        if (!existingCategory.getName().equals(category.getName()) && 
            categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        
        return categoryRepository.save(existingCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
    
    /**
     * Initializes jewelry categories when the application starts
     */
    @Override
    @Transactional
    public void run(String... args) {
        // Only add categories if none exist
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping initialization");
            return;
        }

        log.info("Initializing jewelry categories...");
        
        // Create the jewelry categories specified by the user
        List<Category> categories = Arrays.asList(
            createCategory("Bagues", "Belles bagues en or, argent et autres matériaux précieux"),
            createCategory("Collier", "Colliers élégants pour toutes les occasions"),
            createCategory("Bracelets", "Bracelets stylés pour hommes et femmes"),
            createCategory("Boucle d'oreilles", "Boucles d'oreilles tendance et classiques"),
            createCategory("Chaine de cheville", "Chaines de cheville délicates et raffinées"),
            createCategory("Montres", "Montres de luxe et montres tendance")
        );
        
        // Save all categories
        categoryRepository.saveAll(categories);
        log.info("Added {} jewelry categories successfully", categories.size());
    }
    
    private Category createCategory(String name, String description) {
        return Category.builder()
            .name(name)
            .description(description)
            .build();
    }
}
