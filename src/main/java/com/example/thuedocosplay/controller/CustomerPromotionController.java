package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.PromotionResponse;
import com.example.thuedocosplay.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class CustomerPromotionController {

    private final PromotionService promotionService;

    @GetMapping("/check")
    public ApiResponse<PromotionResponse> checkPromotion(
            @RequestParam String code,
            @RequestParam BigDecimal cartTotal) {
        return ApiResponse.ok(promotionService.checkAndApplyPromotion(code, cartTotal));
    }
}
