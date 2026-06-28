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
    private Long categoryId;
    private String categoryName;
    private String description;
    private BigDecimal pricePerDay;
    private BigDecimal deposit;
    private String imageUrl;
    private Boolean visible;

    private Double avgRating;
    private Long reviewCount;

    private List<String> tags;
    private List<String> includes;
}