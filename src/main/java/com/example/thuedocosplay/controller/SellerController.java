package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.request.UpsertProductRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.SellerDashboardResponse;
import com.example.thuedocosplay.dto.response.SellerOrderResponse;
import com.example.thuedocosplay.dto.response.SellerProductResponse;
import com.example.thuedocosplay.dto.response.SellerRevenueResponse;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/dashboard")
    public ApiResponse<SellerDashboardResponse> dashboard(Principal principal) {
        return ApiResponse.ok(sellerService.dashboard(currentEmail(principal)));
    }

    @GetMapping("/products")
    public ApiResponse<List<SellerProductResponse>> listProducts(Principal principal) {
        return ApiResponse.ok(sellerService.listProducts(currentEmail(principal)));
    }

    @PostMapping("/products")
    public ApiResponse<SellerProductResponse> createProduct(
            Principal principal,
            @Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.ok(sellerService.createProduct(currentEmail(principal), request));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<SellerProductResponse> updateProduct(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.ok(sellerService.updateProduct(currentEmail(principal), id, request));
    }

    @PatchMapping("/products/{id}/toggle")
    public ApiResponse<SellerProductResponse> toggleProduct(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok(sellerService.toggleProduct(currentEmail(principal), id));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(Principal principal, @PathVariable Long id) {
        sellerService.deleteProduct(currentEmail(principal), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/orders")
    public ApiResponse<List<SellerOrderResponse>> listOrders(
            Principal principal,
            @RequestParam(required =false) OrderStatus status,
            @RequestParam(required =false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required =false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required =false)
            String keyword
    ) {
        return ApiResponse.ok(
                sellerService.listOrders(
                        currentEmail(principal),
                        status,
                        fromDate,
                        toDate,
                        keyword
                )
        );
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<SellerOrderResponse> getOrder(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok(sellerService.getOrder(currentEmail(principal), id));
    }

    @PatchMapping("/orders/{id}/status")
    public ApiResponse<SellerOrderResponse> updateOrderStatus(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.ok(sellerService.updateOrderStatus(currentEmail(principal), id, request));
    }

    @GetMapping("/revenue")
    public ApiResponse<SellerRevenueResponse> revenue(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ApiResponse.ok(sellerService.revenue(currentEmail(principal), fromDate, toDate, groupBy));
    }

    private String currentEmail(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
