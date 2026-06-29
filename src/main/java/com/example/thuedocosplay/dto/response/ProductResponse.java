package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String categoryName;
    private Long categoryId;
    private Long sellerId;
    private String sellerName;
    private String description;
    private BigDecimal pricePerDay;
    private BigDecimal deposit;
    private String imageUrl;
    private Boolean visible;
    private Integer quantity;
    private ProductInventoryStatus inventoryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // UI specific fields from products.js
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private List<String> includes;
}
