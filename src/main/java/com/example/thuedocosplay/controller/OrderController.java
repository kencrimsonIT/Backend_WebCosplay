package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateOrderRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ApiResponse.ok(orderService.createOrder(request, currentUserEmail));
    }

    @GetMapping("/my-history")
    public ApiResponse<List<OrderResponse>> myHistory(
            @RequestParam(required = false) com.example.thuedocosplay.entity.enums.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ApiResponse.ok(orderService.getMyOrderHistory(currentUserEmail, status, fromDate, toDate));
    }

    @GetMapping("/my-history/{id}")
    public ApiResponse<OrderResponse> myHistoryDetail(@PathVariable Long id, Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ApiResponse.ok(orderService.getMyOrderDetail(currentUserEmail, id));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getOrder(id));
    }
}
