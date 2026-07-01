package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ProductFilterRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.PagedResponse;
import com.example.thuedocosplay.dto.response.ProductResponse;
import com.example.thuedocosplay.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * THAY THẾ ProductController cũ.
 *
 * Endpoints:
 *   GET /api/products                    → danh sách có filter + sort + phân trang
 *   GET /api/products/{id}               → chi tiết sản phẩm
 *   GET /api/products/homepage/newest    → 8 sản phẩm mới nhất (trang chủ)
 *   GET /api/products/homepage/bestseller → 8 sản phẩm bán chạy nhất (trang chủ)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Danh sách sản phẩm — filter + sort + phân trang.
     *
     * Query params:
     *   categoryId  = 1
     *   keyword     = naruto
     *   minPrice    = 50000
     *   maxPrice    = 300000
     *   sort        = newest | oldest | price_asc | price_desc | best_seller
     *   page        = 0
     *   size        = 12
     */
    @GetMapping
    public ApiResponse<PagedResponse<ProductResponse>> list(ProductFilterRequest filter) {
        return ApiResponse.ok(productService.getProducts(filter));
    }

    /**
     * Chi tiết sản phẩm.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }

    /**
     * Trang chủ — sản phẩm mới nhất.
     * GET /api/products/homepage/newest?limit=8
     */
    @GetMapping("/homepage/newest")
    public ApiResponse<List<ProductResponse>> newest(
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.ok(productService.getNewestProducts(limit));
    }

    /**
     * Trang chủ — sản phẩm bán chạy nhất.
     * GET /api/products/homepage/bestseller?limit=8
     */
    @GetMapping("/homepage/bestseller")
    public ApiResponse<List<ProductResponse>> bestSeller(
            @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.ok(productService.getBestSellerProducts(limit));
    }
}
