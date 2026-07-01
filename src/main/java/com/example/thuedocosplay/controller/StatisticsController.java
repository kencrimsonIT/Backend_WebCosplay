package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.DashboardSummaryResponse;
import com.example.thuedocosplay.dto.response.RevenueTimelineResponse;
import com.example.thuedocosplay.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * StatisticsController đã gộp:
 * 1. Doanh thu theo danh mục (revenue-by-category).
 * 2. Tổng quan dashboard (dashboard).
 * 3. Doanh thu theo thời gian (revenue).
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 1. Doanh thu theo danh mục
    @GetMapping("/revenue-by-category")
    public ApiResponse<CategoryRevenueResponse> revenueByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ApiResponse.ok(statisticsService.revenueByCategory(y, m));
    }

    // 2. Tổng quan Dashboard
    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ApiResponse.ok(statisticsService.dashboardSummary(y, m));
    }

    // 3. Doanh thu theo mốc thời gian
    @GetMapping("/revenue")
    public ApiResponse<RevenueTimelineResponse> revenueTimeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ApiResponse.ok(statisticsService.revenueTimeline(fromDate, toDate, groupBy));
    }
}