package com.example.springnodebackend.payload.response;

import com.example.springnodebackend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    private List<Product> products;
    private int count;
    private String query;
    private SearchFilters filters;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilters {
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }
}
