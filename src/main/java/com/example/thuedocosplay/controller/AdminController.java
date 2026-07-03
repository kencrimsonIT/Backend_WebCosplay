package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.request.UpsertCategoryRequest;
import com.example.thuedocosplay.dto.request.UpsertProductRequest;
import com.example.thuedocosplay.dto.request.UpsertUserRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CategoryResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.service.AdminService;
import com.example.thuedocosplay.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    // Users
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> listUsers() {
        return ApiResponse.ok(adminService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Object>> createUser(@Valid @RequestBody UpsertUserRequest request) {
        return ApiResponse.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody UpsertUserRequest request) {
        return ApiResponse.ok(adminService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/toggle")
    public ApiResponse<Map<String, Object>> toggleUser(@PathVariable Long id) {
        return ApiResponse.ok(adminService.toggleUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok(null);
    }

    // Categories
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> listCategories() {
        return ApiResponse.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody UpsertCategoryRequest request) {
        return ApiResponse.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody UpsertCategoryRequest request) {
        return ApiResponse.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ApiResponse.ok(null);
    }

    // Products
    @GetMapping("/products")
    public ApiResponse<List<Map<String, Object>>> listProducts() {
        return ApiResponse.ok(adminService.listProducts());
    }

    @PostMapping("/products")
    public ApiResponse<Map<String, Object>> createProduct(@Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.ok(adminService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(@PathVariable Long id, @Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.ok(adminService.updateProduct(id, request));
    }

    @PatchMapping("/products/{id}/toggle")
    public ApiResponse<Map<String, Object>> toggleProduct(@PathVariable Long id) {
        return ApiResponse.ok(adminService.toggleProductVisibility(id));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ApiResponse.ok(null);
    }

    // Orders
    @GetMapping("/orders")
    public ApiResponse<List<OrderResponse>> listOrders() {
        return ApiResponse.ok(orderService.listOrders());
    }

    @PatchMapping("/orders/{id}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.ok(orderService.updateStatus(id, request));
    }
}
