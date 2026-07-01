package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String categoryName;
    private String productImageUrl;
    private String size;
    private Integer days;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
