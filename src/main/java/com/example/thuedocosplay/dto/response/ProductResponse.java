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
    // Thông tin cơ bản
    private Long id;
    private String name;
    
    // Phân loại & Người bán
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    
    // Thông tin chi tiết & Giá
    private String description;
    private BigDecimal pricePerDay;
    private BigDecimal deposit;
    private String imageUrl;
    
    // Trạng thái & Quản lý
    private Boolean visible;
    private Integer quantity;
    private ProductInventoryStatus inventoryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Đánh giá (Kết hợp logic cả 2 nhánh)
    private Double rating;      // Từ nhánh của bạn
    private Integer reviewCount;      // Từ nhánh của bạn
    private List<ProductReviewResponse> reviews; // Từ nhánh main
    
    // Thông tin bổ sung
    private List<String> tags;
    private List<String> includes;
}