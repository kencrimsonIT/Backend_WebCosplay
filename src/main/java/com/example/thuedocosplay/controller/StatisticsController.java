package com.example.thuedocosplay.controller;

import com.example.thuedocosplay.dto.response.ApiResponse;
import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.RevenueTimelineResponse;
import com.example.thuedocosplay.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/revenue-by-category")
    public ApiResponse<CategoryRevenueResponse> revenueByCategory(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ApiResponse.ok(statisticsService.revenueByCategory(y, m));
    }

    @GetMapping("/revenue")
    public ApiResponse<RevenueTimelineResponse> revenueTimeline(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ApiResponse.ok(statisticsService.revenueTimeline(fromDate, toDate, groupBy));
    }
}
