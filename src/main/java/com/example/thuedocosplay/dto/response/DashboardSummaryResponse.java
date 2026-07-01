package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response cho dashboard tổng quan của admin.
 *
 * GET /api/statistics/dashboard?year=2026&month=6
 */
@Data
@Builder
public class DashboardSummaryResponse {

    private int year;
    private int month;

    // Tổng doanh thu tháng (chỉ đơn đã thanh toán)
    private BigDecimal totalRevenue;

    // Tổng số đơn trong tháng (theo từng trạng thái)
    private long totalOrders;
    private long pendingOrders;      // PENDING_PAYMENT + PENDING_CONFIRM
    private long confirmedOrders;    // CONFIRMED + RENTING
    private long completedOrders;    // COMPLETED
    private long cancelledOrders;    // CANCELLED

    // Biểu đồ tròn doanh thu theo danh mục
    private CategoryRevenueResponse revenueByCategory;

    // Top 5 sản phẩm doanh thu cao nhất tháng
    private List<TopProductItem> topProducts;

    @Data
    @Builder
    public static class TopProductItem {
        private String productName;
        private String categoryName;
        private long totalQuantity;
        private BigDecimal totalRevenue;
    }
}
