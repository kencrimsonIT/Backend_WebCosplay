package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ProductResponse;
import com.example.thuedocosplay.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> list() {
        return ApiResponse.ok(productService.getAllVisibleProducts());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }
}
