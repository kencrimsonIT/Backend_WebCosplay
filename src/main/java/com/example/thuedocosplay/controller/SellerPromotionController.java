package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.UpsertPromotionRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.PromotionResponse;
import com.example.thuedocosplay.dto.response.PromotionSummaryResponse;
import com.example.thuedocosplay.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/seller/promotions")
@RequiredArgsConstructor
public class SellerPromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ApiResponse<List<PromotionResponse>> list(Principal principal) {
        return ApiResponse.ok(promotionService.listBySeller(email(principal)));
    }

    @GetMapping("/summary")
    public ApiResponse<PromotionSummaryResponse> summary(Principal principal) {
        PromotionService.PromotionSummary s = promotionService.getSummary(email(principal));
        return ApiResponse.ok(new PromotionSummaryResponse(s.active(), s.totalUsed(), s.expiringSoon()));
    }

    @PostMapping
    public ApiResponse<PromotionResponse> create(
            Principal principal,
            @Valid @RequestBody UpsertPromotionRequest request) {
        return ApiResponse.ok(promotionService.create(email(principal), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpsertPromotionRequest request) {
        return ApiResponse.ok(promotionService.update(email(principal), id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ApiResponse<PromotionResponse> toggle(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok(promotionService.toggleStatus(email(principal), id));
    }

    @PostMapping("/{id}/duplicate")
    public ApiResponse<PromotionResponse> duplicate(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok(promotionService.duplicate(email(principal), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Principal principal, @PathVariable Long id) {
        promotionService.delete(email(principal), id);
        return ApiResponse.ok(null);
    }

    private String email(Principal p) {
        return p != null ? p.getName() : null;
    }
}
