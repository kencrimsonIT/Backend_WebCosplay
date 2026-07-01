package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.DashboardSummaryResponse;
import com.example.thuedocosplay.dto.response.RevenueTimelineResponse;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private static final String[] CHART_COLORS = {
            "#7c3aed", "#ec4899", "#06b6d4", "#f59e0b",
            "#10b981", "#6366f1", "#ef4444", "#f97316", "#14b8a6"
    };

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM, OrderStatus.CONFIRMED, OrderStatus.RENTING, OrderStatus.COMPLETED
    );

    private static final List<OrderStatus> ACTIVE_DEPOSIT_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM, OrderStatus.CONFIRMED, OrderStatus.RENTING
    );

    private final RentalOrderRepository orderRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD & DOANH THU DANH MỤC
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        long totalOrders = orderRepository.countByCreatedAtBetween(from, to);
        long pendingOrders = orderRepository.countByStatusInAndCreatedAtBetween(List.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PENDING_CONFIRM), from, to);
        long confirmedOrders = orderRepository.countByStatusInAndCreatedAtBetween(List.of(OrderStatus.CONFIRMED, OrderStatus.RENTING), from, to);
        long completedOrders = orderRepository.countByStatusInAndCreatedAtBetween(List.of(OrderStatus.COMPLETED), from, to);
        long cancelledOrders = orderRepository.countByStatusInAndCreatedAtBetween(List.of(OrderStatus.CANCELLED), from, to);

        BigDecimal totalRevenue = orderRepository.sumRevenue(REVENUE_STATUSES, from, to);
        CategoryRevenueResponse revenueByCategory = buildCategoryRevenue(year, month, from, to, totalRevenue);

        var topRows = orderRepository.topProductsByRevenue(from, to, REVENUE_STATUSES, PageRequest.of(0, 5));
        List<DashboardSummaryResponse.TopProductItem> topProducts = topRows.stream()
                .map(r -> DashboardSummaryResponse.TopProductItem.builder()
                        .productName(r.getProductName()).categoryName(r.getCategoryName())
                        .totalQuantity(r.getTotalQuantity()).totalRevenue(r.getTotalRevenue()).build())
                .toList();

        return DashboardSummaryResponse.builder()
                .year(year).month(month).totalRevenue(totalRevenue == null ? BigDecimal.ZERO : totalRevenue)
                .totalOrders(totalOrders).pendingOrders(pendingOrders).confirmedOrders(confirmedOrders)
                .completedOrders(completedOrders).cancelledOrders(cancelledOrders)
                .revenueByCategory(revenueByCategory).topProducts(topProducts).build();
    }

    @Transactional(readOnly = true)
    public CategoryRevenueResponse revenueByCategory(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();
        BigDecimal total = orderRepository.sumRevenue(REVENUE_STATUSES, from, to);
        return buildCategoryRevenue(year, month, from, to, total == null ? BigDecimal.ZERO : total);
    }

    private CategoryRevenueResponse buildCategoryRevenue(int year, int month, LocalDateTime from, LocalDateTime to, BigDecimal totalRevenue) {
        var rows = orderRepository.sumRevenueByCategoryInMonth(from, to, REVENUE_STATUSES);
        List<CategoryRevenueResponse.Slice> slices = new ArrayList<>();
        int colorIdx = 0;
        for (var row : rows) {
            BigDecimal revenue = row.getRevenue();
            double pct = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : revenue.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP).doubleValue();
            slices.add(CategoryRevenueResponse.Slice.builder()
                    .categoryName(row.getCategoryName()).revenue(revenue).percentage(pct)
                    .color(CHART_COLORS[colorIdx++ % CHART_COLORS.length]).build());
        }
        return CategoryRevenueResponse.builder().year(year).month(month).totalRevenue(totalRevenue).slices(slices).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DOANH THU TIMELINE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RevenueTimelineResponse revenueTimeline(LocalDate fromDate, LocalDate toDate, String groupBy) {
        LocalDate now = LocalDate.now();
        LocalDate resolvedTo = toDate != null ? toDate : now;
        LocalDate resolvedFrom = fromDate != null ? fromDate : resolvedTo.minusDays(29);
        RevenueGroup group = RevenueGroup.from(groupBy);
        validateRange(resolvedFrom, resolvedTo, group);

        LocalDateTime fromInclusive = resolvedFrom.atStartOfDay();
        LocalDateTime toExclusive = resolvedTo.plusDays(1).atStartOfDay();
        List<RentalOrder> orders = orderRepository.findRevenueOrders(fromInclusive, toExclusive, REVENUE_STATUSES);

        Map<String, Bucket> buckets = createBuckets(resolvedFrom, resolvedTo, group);
        Map<String, PaymentBucket> paymentBuckets = new LinkedHashMap<>();

        for (RentalOrder order : orders) {
            LocalDate paidDate = order.getPaidAt().toLocalDate();
            String bucketKey = group.key(paidDate);
            Bucket bucket = buckets.get(bucketKey);
            if (bucket != null) {
                bucket.add(order);
                paymentBuckets.computeIfAbsent(order.getPaymentMethod().name(), PaymentBucket::new).add(order);
            }
        }

        SummaryAccumulator summary = new SummaryAccumulator();
        orders.forEach(summary::add);

        return RevenueTimelineResponse.builder()
                .fromDate(resolvedFrom).toDate(resolvedTo).groupBy(group.value)
                .summary(RevenueTimelineResponse.Summary.builder()
                        .totalRevenue(summary.totalRevenue).rentalRevenue(summary.rentalRevenue)
                        .depositCollected(summary.depositCollected).depositHeld(summary.depositHeld)
                        .warrantyRevenue(summary.warrantyRevenue).averageOrderValue(summary.averageOrderValue())
                        .orderCount(summary.orderCount).completedOrderCount(summary.completedOrderCount)
                        .activeDepositOrderCount(summary.activeDepositOrderCount).build())
                .bars(buckets.values().stream().map(Bucket::toBar).toList())
                .paymentMethods(paymentBuckets.values().stream()
                        .sorted(Comparator.comparing(PaymentBucket::getTotalRevenue).reversed())
                        .map(b -> b.toResponse(summary.totalRevenue)).toList())
                .build();
    }

    private Map<String, Bucket> createBuckets(LocalDate fromDate, LocalDate toDate, RevenueGroup group) {
        Map<String, Bucket> buckets = new LinkedHashMap<>();
        LocalDate cursor = group.periodStart(fromDate);
        LocalDate last = group.periodStart(toDate);
        while (!cursor.isAfter(last)) {
            LocalDate start = cursor.isBefore(fromDate) ? fromDate : cursor;
            LocalDate end = group.next(cursor).minusDays(1);
            end = end.isAfter(toDate) ? toDate : end;
            Bucket bucket = new Bucket(group.key(cursor), group.label(cursor), start, end);
            buckets.put(bucket.key, bucket);
            cursor = group.next(cursor);
        }
        return buckets;
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate, RevenueGroup group) {
        if (fromDate.isAfter(toDate)) throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        long maxDays = (group == RevenueGroup.DAY) ? 370 : 36600;
        if (fromDate.plusDays(maxDays).isBefore(toDate)) throw new IllegalArgumentException("Khoảng thời gian quá lớn");
    }

    private static BigDecimal safe(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    // ─────────────────────────────────────────────────────────────────────────
    // INNER CLASSES (Bucket, Accumulator, Enum...)
    // ─────────────────────────────────────────────────────────────────────────

    private enum RevenueGroup {
        DAY("day"), MONTH("month"), YEAR("year");
        private final String value;
        RevenueGroup(String v) { this.value = v; }
        static RevenueGroup from(String v) { return Arrays.stream(values()).filter(g -> g.value.equalsIgnoreCase(v)).findFirst().orElse(DAY); }
        String key(LocalDate d) { return switch (this) { case MONTH -> d.format(DateTimeFormatter.ofPattern("yyyy-MM")); case YEAR -> String.valueOf(d.getYear()); default -> d.toString(); }; }
        String label(LocalDate d) { return switch (this) { case MONTH -> d.format(DateTimeFormatter.ofPattern("MM/yyyy")); case YEAR -> String.valueOf(d.getYear()); default -> d.format(DateTimeFormatter.ofPattern("dd/MM")); }; }
        LocalDate periodStart(LocalDate d) { return switch (this) { case MONTH -> d.withDayOfMonth(1); case YEAR -> d.withDayOfYear(1); default -> d; }; }
        LocalDate next(LocalDate d) { return switch (this) { case MONTH -> d.plusMonths(1).withDayOfMonth(1); case YEAR -> d.plusYears(1).withDayOfYear(1); default -> d.plusDays(1); }; }
    }

    private static class SummaryAccumulator {
        BigDecimal totalRevenue = BigDecimal.ZERO, rentalRevenue = BigDecimal.ZERO, depositCollected = BigDecimal.ZERO, depositHeld = BigDecimal.ZERO, warrantyRevenue = BigDecimal.ZERO;
        long orderCount, completedOrderCount, activeDepositOrderCount;
        void add(RentalOrder o) {
            totalRevenue = totalRevenue.add(safe(o.getGrandTotal()));
            rentalRevenue = rentalRevenue.add(safe(o.getRentalTotal()));
            depositCollected = depositCollected.add(safe(o.getDepositTotal()));
            warrantyRevenue = warrantyRevenue.add(safe(o.getWarrantyTotal()));
            orderCount++;
            if (o.getStatus() == OrderStatus.COMPLETED) completedOrderCount++;
            if (ACTIVE_DEPOSIT_STATUSES.contains(o.getStatus())) { depositHeld = depositHeld.add(safe(o.getDepositTotal())); activeDepositOrderCount++; }
        }
        BigDecimal averageOrderValue() { return orderCount == 0 ? BigDecimal.ZERO : totalRevenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP); }
    }

    private static class Bucket extends SummaryAccumulator {
        private final String key, label;
        private final LocalDate periodStart, periodEnd;
        Bucket(String k, String l, LocalDate s, LocalDate e) { this.key = k; this.label = l; this.periodStart = s; this.periodEnd = e; }
        RevenueTimelineResponse.Bar toBar() { return RevenueTimelineResponse.Bar.builder().key(key).label(label).periodStart(periodStart).periodEnd(periodEnd).totalRevenue(totalRevenue).rentalRevenue(rentalRevenue).depositCollected(depositCollected).warrantyRevenue(warrantyRevenue).orderCount(orderCount).completedOrderCount(completedOrderCount).build(); }
    }

    private static class PaymentBucket {
        private final String method;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private long orderCount;
        PaymentBucket(String m) { this.method = m; }
        void add(RentalOrder o) { totalRevenue = totalRevenue.add(safe(o.getGrandTotal())); orderCount++; }
        BigDecimal getTotalRevenue() { return totalRevenue; }
        RevenueTimelineResponse.PaymentMethodBreakdown toResponse(BigDecimal total) { return RevenueTimelineResponse.PaymentMethodBreakdown.builder().method(method).totalRevenue(totalRevenue).orderCount(orderCount).percentage(total.compareTo(BigDecimal.ZERO) == 0 ? 0 : totalRevenue.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).doubleValue()).build(); }
    }
}