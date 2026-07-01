package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CategoryResponse;
import com.example.thuedocosplay.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.getActiveCategories());
    }
}
