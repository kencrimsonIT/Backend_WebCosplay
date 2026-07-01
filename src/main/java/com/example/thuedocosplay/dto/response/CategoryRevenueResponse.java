package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CategoryRevenueResponse {
    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private List<Slice> slices;

    @Data
    @Builder
    public static class Slice {
        private String categoryName;
        private BigDecimal revenue;
        private double percentage;
        private String color;
    }
}
