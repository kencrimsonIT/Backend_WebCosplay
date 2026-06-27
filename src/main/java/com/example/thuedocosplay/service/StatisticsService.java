package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.DashboardSummaryResponse;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final String[] CHART_COLORS = {
            "#7c3aed", "#ec4899", "#06b6d4", "#f59e0b",
            "#10b981", "#6366f1", "#ef4444", "#f97316", "#14b8a6"
    };

    // Trạng thái tính vào doanh thu (đã xác nhận trở lên)
    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM,
            OrderStatus.CONFIRMED,
            OrderStatus.RENTING,
            OrderStatus.COMPLETED
    );

    private final RentalOrderRepository orderRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Dashboard tổng quan
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary(int year, int month) {
        YearMonth ym   = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to   = ym.plusMonths(1).atDay(1).atStartOfDay();

        // ── Tổng số đơn ──────────────────────────────────────────────────────
        long totalOrders = orderRepository.countByCreatedAtBetween(from, to);

        long pendingOrders = orderRepository.countByStatusInAndCreatedAtBetween(
                List.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PENDING_CONFIRM), from, to);

        long confirmedOrders = orderRepository.countByStatusInAndCreatedAtBetween(
                List.of(OrderStatus.CONFIRMED, OrderStatus.RENTING), from, to);

        long completedOrders = orderRepository.countByStatusInAndCreatedAtBetween(
                List.of(OrderStatus.COMPLETED), from, to);

        long cancelledOrders = orderRepository.countByStatusInAndCreatedAtBetween(
                List.of(OrderStatus.CANCELLED), from, to);

        // ── Tổng doanh thu ───────────────────────────────────────────────────
        BigDecimal totalRevenue = orderRepository.sumRevenue(REVENUE_STATUSES, from, to);

        // ── Biểu đồ tròn doanh thu theo danh mục ────────────────────────────
        CategoryRevenueResponse revenueByCategory = buildCategoryRevenue(year, month, from, to, totalRevenue);

        // ── Top 5 sản phẩm ───────────────────────────────────────────────────
        var topRows = orderRepository.topProductsByRevenue(
                from, to, REVENUE_STATUSES, PageRequest.of(0, 5));

        List<DashboardSummaryResponse.TopProductItem> topProducts = topRows.stream()
                .map(r -> DashboardSummaryResponse.TopProductItem.builder()
                        .productName(r.getProductName())
                        .categoryName(r.getCategoryName())
                        .totalQuantity(r.getTotalQuantity())
                        .totalRevenue(r.getTotalRevenue())
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .year(year)
                .month(month)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .revenueByCategory(revenueByCategory)
                .topProducts(topProducts)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Doanh thu theo danh mục (giữ nguyên — dùng độc lập)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CategoryRevenueResponse revenueByCategory(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to   = ym.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal total = orderRepository.sumRevenue(REVENUE_STATUSES, from, to);
        return buildCategoryRevenue(year, month, from, to, total);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private CategoryRevenueResponse buildCategoryRevenue(
            int year, int month,
            LocalDateTime from, LocalDateTime to,
            BigDecimal totalRevenue) {

        var rows = orderRepository.sumRevenueByCategoryInMonth(from, to, REVENUE_STATUSES);

        List<CategoryRevenueResponse.Slice> slices = new ArrayList<>();
        int colorIdx = 0;

        for (var row : rows) {
            BigDecimal revenue = row.getRevenue();
            double pct = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : revenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                    .doubleValue();

            slices.add(CategoryRevenueResponse.Slice.builder()
                    .categoryName(row.getCategoryName())
                    .revenue(revenue)
                    .percentage(pct)
                    .color(CHART_COLORS[colorIdx % CHART_COLORS.length])
                    .build());
            colorIdx++;
        }

        return CategoryRevenueResponse.builder()
                .year(year)
                .month(month)
                .totalRevenue(totalRevenue)
                .slices(slices)
                .build();
    }
}
