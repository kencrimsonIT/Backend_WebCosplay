// ═══════════════════════════════════════════════════════════════════════════
// FILE: OrderController.java  (THAY THẾ file cũ)
// ═══════════════════════════════════════════════════════════════════════════
package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * THAY THẾ OrderController cũ.
 *
 * Thêm mới:
 *   GET  /api/orders/my            → lấy đơn hàng của user đang đăng nhập
 *   POST /api/orders/{id}/cancel   → user hủy đơn (chỉ khi PENDING_PAYMENT hoặc PENDING_CONFIRM)
 *   PATCH /api/orders/{id}/status  → admin cập nhật trạng thái (giữ từ AdminController)
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Tạo đơn hàng
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.createOrder(request));
    }

    // Xem chi tiết 1 đơn
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrder(id));
    }

    // [MỚI] Lấy tất cả đơn của user đang đăng nhập
    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(orderService.getOrdersByUserEmail(userDetails.getUsername()));
    }

    // [MỚI] User hủy đơn hàng
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ApiResponse.ok(orderService.cancelOrderByUser(id, userDetails.getUsername(), reason));
    }
}
