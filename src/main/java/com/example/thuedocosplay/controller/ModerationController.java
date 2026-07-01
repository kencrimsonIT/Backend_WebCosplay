package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ModerationActionRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.ModerationReviewResponse;
import com.example.thuedocosplay.dto.response.ModerationStatsResponse;
import com.example.thuedocosplay.dto.response.PagedResponse;
import com.example.thuedocosplay.entity.enums.ModerationStatus;
import com.example.thuedocosplay.service.ModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModerationController {

    private final ModerationService moderationService;
    @GetMapping("/reviews")
    public ApiResponse<PagedResponse<ModerationReviewResponse>> listReviews(
            @RequestParam(required = false) ModerationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(moderationService.listReviews(status, page, size));
    }
    @PatchMapping("/reviews/{id}")
    public ApiResponse<ModerationReviewResponse> moderate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin,
            @Valid @RequestBody ModerationActionRequest request) {
        return ApiResponse.ok(moderationService.moderate(id, admin.getUsername(), request));
    }
    @GetMapping("/stats")
    public ApiResponse<ModerationStatsResponse> stats() {
        return ApiResponse.ok(moderationService.getStats());
    }

    @GetMapping("/banned-keywords")
    public ApiResponse<String> getBannedKeywords() {
        return ApiResponse.ok(moderationService.getBannedKeywords());
    }
    public record KeywordRequest(String keywords) {}

    @PutMapping("/banned-keywords")
    public ApiResponse<String> updateBannedKeywords(@RequestBody KeywordRequest req) {
        moderationService.updateBannedKeywords(req.keywords());
        return ApiResponse.ok("Cập nhật từ khóa cấm thành công");
    }
}