package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
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
            "#7c3aed", "#ec4899", "#06b6d4", "#f59e0b", "#10b981", "#6366f1", "#ef4444"
    };

    private final RentalOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public CategoryRevenueResponse revenueByCategory(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<OrderStatus> statuses = List.of(
                OrderStatus.PENDING_CONFIRM,
                OrderStatus.CONFIRMED,
                OrderStatus.RENTING,
                OrderStatus.COMPLETED
        );

        var rows = orderRepository.sumRevenueByCategoryInMonth(from, to, statuses);

        BigDecimal total = rows.stream()
                .map(RentalOrderRepository.CategoryRevenueProjection::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryRevenueResponse.Slice> slices = new ArrayList<>();
        int colorIdx = 0;

        for (var row : rows) {
            BigDecimal revenue = row.getRevenue();
            double pct = total.compareTo(BigDecimal.ZERO) == 0
                    ? 0
                    : revenue.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP)
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
                .totalRevenue(total)
                .slices(slices)
                .build();
    }
}
