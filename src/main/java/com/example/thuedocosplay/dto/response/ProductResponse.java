package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String categoryName;
    private Long categoryId;
    private String description; // Add if needed, but not in current entity
    private BigDecimal pricePerDay;
    private BigDecimal deposit;
    private String imageUrl;
    private Boolean visible;
    
    // UI specific fields from products.js
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private List<String> includes;
}
