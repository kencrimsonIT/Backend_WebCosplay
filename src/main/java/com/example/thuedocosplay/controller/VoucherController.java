package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.request.ApplyVoucherRequest;
import com.example.thuedocosplay.dto.request.UpsertVoucherRequest;
import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.PromotionSummaryResponse;
import com.example.thuedocosplay.dto.response.VoucherApplyResponse;
import com.example.thuedocosplay.dto.response.VoucherResponse;
import com.example.thuedocosplay.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/api/vouchers/apply")
    public ApiResponse<VoucherApplyResponse> applyVoucher(
            Principal principal,
            @Valid @RequestBody ApplyVoucherRequest request
    ) {
        return ApiResponse.ok(voucherService.previewVoucher(currentEmail(principal), request));
    }

    @GetMapping("/api/seller/vouchers")
    public ApiResponse<List<VoucherResponse>> listSellerVouchers(Principal principal) {
        return ApiResponse.ok(voucherService.listSellerVouchers(currentEmail(principal)));
    }

    @PostMapping("/api/seller/vouchers")
    public ApiResponse<VoucherResponse> createSellerVoucher(
            Principal principal,
            @Valid @RequestBody UpsertVoucherRequest request
    ) {
        return ApiResponse.ok("Da tao voucher", voucherService.createSellerVoucher(currentEmail(principal), request));
    }

    @PutMapping("/api/seller/vouchers/{id}")
    public ApiResponse<VoucherResponse> updateSellerVoucher(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpsertVoucherRequest request
    ) {
        return ApiResponse.ok("Da cap nhat voucher", voucherService.updateSellerVoucher(currentEmail(principal), id, request));
    }

    @PatchMapping("/api/seller/vouchers/{id}/toggle")
    public ApiResponse<VoucherResponse> toggleSellerVoucher(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok("Da doi trang thai voucher", voucherService.toggleSellerVoucher(currentEmail(principal), id));
    }

    @PostMapping("/api/seller/vouchers/{id}/duplicate")
    public ApiResponse<VoucherResponse> duplicateSellerVoucher(Principal principal, @PathVariable Long id) {
        return ApiResponse.ok("Da copy voucher", voucherService.duplicateSellerVoucher(currentEmail(principal), id));
    }

    @DeleteMapping("/api/seller/vouchers/{id}")
    public ApiResponse<Void> deleteSellerVoucher(Principal principal, @PathVariable Long id) {
        voucherService.deleteSellerVoucher(currentEmail(principal), id);
        return ApiResponse.ok("Da xoa voucher", null);
    }

    @GetMapping("/api/seller/vouchers/summary")
    public ApiResponse<PromotionSummaryResponse> voucherSummary(Principal principal) {
        VoucherService.VoucherSummary summary = voucherService.getSellerSummary(currentEmail(principal));
        return ApiResponse.ok(new PromotionSummaryResponse(summary.active(), summary.totalUsed(), summary.expiringSoon()));
    }

    private String currentEmail(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
