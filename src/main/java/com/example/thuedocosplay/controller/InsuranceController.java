package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.CreateClaimRequest;
import com.example.thuedocosplay.dto.request.CreateInsurancePlanRequest;
import com.example.thuedocosplay.dto.request.ResolveClaimRequest;
import com.example.thuedocosplay.dto.response.*;
import com.example.thuedocosplay.entity.InsuranceClaim.ClaimStatus;
import com.example.thuedocosplay.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    // ─── PUBLIC: danh sách gói (dùng ở trang checkout) ──────────────────────

    @GetMapping("/api/insurance/plans")
    public ApiResponse<List<InsurancePlanResponse>> publicPlans() {
        return ApiResponse.ok(insuranceService.getActivePlans());
    }

    @GetMapping("/api/admin/insurance/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<InsurancePlanResponse>> adminListPlans() {
        return ApiResponse.ok(insuranceService.getAllPlans());
    }

    @PostMapping("/api/admin/insurance/plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsurancePlanResponse> createPlan(
            @Valid @RequestBody CreateInsurancePlanRequest req) {
        return ApiResponse.ok(insuranceService.createPlan(req));
    }

    @PutMapping("/api/admin/insurance/plans/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsurancePlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody CreateInsurancePlanRequest req) {
        return ApiResponse.ok(insuranceService.updatePlan(id, req));
    }

    @PatchMapping("/api/admin/insurance/plans/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsurancePlanResponse> togglePlan(@PathVariable Long id) {
        return ApiResponse.ok(insuranceService.togglePlan(id));
    }

    // ─── ADMIN: quản lý claim ────────────────────────────────────────────────

    @GetMapping("/api/admin/insurance/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PagedResponse<InsuranceClaimResponse>> listClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(insuranceService.listClaims(status, page, size));
    }

    @PostMapping("/api/admin/insurance/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsuranceClaimResponse> createClaim(
            @AuthenticationPrincipal UserDetails admin,
            @Valid @RequestBody CreateClaimRequest req) {
        return ApiResponse.ok(insuranceService.createClaim(admin.getUsername(), req));
    }

    @PatchMapping("/api/admin/insurance/claims/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsuranceClaimResponse> startVerify(@PathVariable Long id) {
        return ApiResponse.ok(insuranceService.startVerifying(id));
    }

    @PatchMapping("/api/admin/insurance/claims/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsuranceClaimResponse> resolveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin,
            @Valid @RequestBody ResolveClaimRequest req) {
        return ApiResponse.ok(insuranceService.resolveClaim(id, admin.getUsername(), req));
    }
    @GetMapping("/api/admin/insurance/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InsuranceStatsResponse> stats() {
        return ApiResponse.ok(insuranceService.getStats());
    }
}
