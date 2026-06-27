package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.DashboardSummaryResponse;
import com.example.thuedocosplay.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * THAY THẾ StatisticsController cũ.
 *
 * Endpoints:
 *   GET /api/statistics/revenue-by-category?year=&month=   (giữ nguyên)
 *   GET /api/statistics/dashboard?year=&month=             (MỚI — tổng hợp dashboard)
 *
 * Cả 2 đều yêu cầu ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;
    @GetMapping("/revenue-by-category")
    public ApiResponse<CategoryRevenueResponse> revenueByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year  != null ? year  : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ApiResponse.ok(statisticsService.revenueByCategory(y, m));
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year  != null ? year  : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ApiResponse.ok(statisticsService.dashboardSummary(y, m));
    }
}
