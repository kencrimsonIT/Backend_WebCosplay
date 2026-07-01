package com.example.thuedocosplay.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Query params cho GET /api/products
 *
 * Ví dụ:
 *   GET /api/products?categoryId=1&minPrice=50000&maxPrice=200000&sort=newest&keyword=naruto&page=0&size=12
 */
@Getter
@Setter
public class ProductFilterRequest {

    // Lọc
    private Long categoryId;
    private String keyword;           // tìm theo tên sản phẩm
    private BigDecimal minPrice;      // pricePerDay >= minPrice
    private BigDecimal maxPrice;      // pricePerDay <= maxPrice

    // Sắp xếp: newest | oldest | price_asc | price_desc | best_seller
    private String sort = "newest";

    // Phân trang
    private int page = 0;
    private int size = 12;
}
