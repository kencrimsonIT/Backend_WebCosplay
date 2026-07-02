package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CartItemRequest;
import com.example.thuedocosplay.dto.request.CartQuantityRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CartResponse;
import com.example.thuedocosplay.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(cartService.getCart(email(userDetails)));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ApiResponse.ok(cartService.addItem(email(userDetails), request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CartQuantityRequest request
    ) {
        return ApiResponse.ok(cartService.updateQuantity(email(userDetails), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return ApiResponse.ok(cartService.removeItem(email(userDetails), id));
    }

    @DeleteMapping
    public ApiResponse<CartResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(cartService.clearCart(email(userDetails)));
    }

    private String email(UserDetails userDetails) {
        return userDetails == null ? null : userDetails.getUsername();
    }
}
