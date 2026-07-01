package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.ProductInventoryStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SellerProductResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
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
}
