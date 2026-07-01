package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.request.UpdateOrderStatusRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * OrderController đã gộp:
 * 1. Các endpoint tạo đơn, chi tiết (Cũ & Mới).
 * 2. Lịch sử đơn hàng cá nhân (my-history).
 * 3. Chức năng hủy đơn hàng của user.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Tạo đơn hàng (Hỗ trợ cả guest và user đăng nhập)
    @PostMapping
    public ApiResponse<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return ApiResponse.ok(orderService.createOrder(request, email));
    }

    // Lịch sử đơn hàng chi tiết (dùng cho user)
    @GetMapping("/my-history")
    public ApiResponse<List<OrderResponse>> myHistory(
            @RequestParam(required = false) com.example.thuedocosplay.entity.enums.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(orderService.getMyOrderHistory(userDetails.getUsername(), status, fromDate, toDate));
    }

    @GetMapping("/my-history/{id}")
    public ApiResponse<OrderResponse> myHistoryDetail(
            @PathVariable Long id, 
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(orderService.getMyOrderDetail(userDetails.getUsername(), id));
    }

    // Xem chi tiết 1 đơn (Dành cho admin hoặc xem công khai tùy cấu hình)
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrder(id));
    }

    // Lấy tất cả đơn của user đang đăng nhập
    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(orderService.getOrdersByUserEmail(userDetails.getUsername()));
    }

    // User hủy đơn hàng
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.getOrDefault("reason", "") : "";
        return ApiResponse.ok(orderService.cancelOrderByUser(id, userDetails.getUsername(), reason));
    }
}