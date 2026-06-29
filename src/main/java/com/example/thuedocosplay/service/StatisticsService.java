package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.CategoryRevenueResponse;
import com.example.thuedocosplay.dto.response.RevenueTimelineResponse;
import com.example.thuedocosplay.entity.RentalOrder;
import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.repository.RentalOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private static final String[] CHART_COLORS = {
            "#7c3aed", "#ec4899", "#06b6d4", "#f59e0b", "#10b981", "#6366f1", "#ef4444"
    };

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM,
            OrderStatus.CONFIRMED,
            OrderStatus.RENTING,
            OrderStatus.COMPLETED
    );

    private static final List<OrderStatus> ACTIVE_DEPOSIT_STATUSES = List.of(
            OrderStatus.PENDING_CONFIRM,
            OrderStatus.CONFIRMED,
            OrderStatus.RENTING
    );

    private final RentalOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public CategoryRevenueResponse revenueByCategory(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        var rows = orderRepository.sumRevenueByCategoryInMonth(from, to, REVENUE_STATUSES);

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
            if (bucket == null) {
                continue;
            }
            bucket.add(order);
            paymentBuckets
                    .computeIfAbsent(order.getPaymentMethod().name(), PaymentBucket::new)
                    .add(order);
        }

        SummaryAccumulator summary = new SummaryAccumulator();
        for (RentalOrder order : orders) {
            summary.add(order);
        }

        RevenueTimelineResponse.Summary summaryResponse = RevenueTimelineResponse.Summary.builder()
                .totalRevenue(summary.totalRevenue)
                .rentalRevenue(summary.rentalRevenue)
                .depositCollected(summary.depositCollected)
                .depositHeld(summary.depositHeld)
                .warrantyRevenue(summary.warrantyRevenue)
                .averageOrderValue(summary.averageOrderValue())
                .orderCount(summary.orderCount)
                .completedOrderCount(summary.completedOrderCount)
                .activeDepositOrderCount(summary.activeDepositOrderCount)
                .build();

        List<RevenueTimelineResponse.Bar> bars = buckets.values().stream()
                .map(Bucket::toBar)
                .toList();

        List<RevenueTimelineResponse.PaymentMethodBreakdown> paymentMethods = paymentBuckets.values().stream()
                .sorted(Comparator.comparing(PaymentBucket::getTotalRevenue).reversed())
                .map(bucket -> bucket.toResponse(summary.totalRevenue))
                .toList();

        log.info(
                "[Statistics] Revenue timeline fromDate={} toDate={} groupBy={} orderCount={} totalRevenue={} rentalRevenue={} depositCollected={} depositHeld={} warrantyRevenue={}",
                resolvedFrom,
                resolvedTo,
                group.value,
                summary.orderCount,
                summary.totalRevenue,
                summary.rentalRevenue,
                summary.depositCollected,
                summary.depositHeld,
                summary.warrantyRevenue
        );

        return RevenueTimelineResponse.builder()
                .fromDate(resolvedFrom)
                .toDate(resolvedTo)
                .groupBy(group.value)
                .summary(summaryResponse)
                .bars(bars)
                .paymentMethods(paymentMethods)
                .build();
    }

    private Map<String, Bucket> createBuckets(LocalDate fromDate, LocalDate toDate, RevenueGroup group) {
        Map<String, Bucket> buckets = new LinkedHashMap<>();
        LocalDate cursor = group.periodStart(fromDate);
        LocalDate last = group.periodStart(toDate);

        while (!cursor.isAfter(last)) {
            LocalDate periodStart = cursor.isBefore(fromDate) ? fromDate : cursor;
            LocalDate rawPeriodEnd = group.next(cursor).minusDays(1);
            LocalDate periodEnd = rawPeriodEnd.isAfter(toDate) ? toDate : rawPeriodEnd;
            Bucket bucket = new Bucket(group.key(cursor), group.label(cursor), periodStart, periodEnd);
            buckets.put(bucket.key, bucket);
            cursor = group.next(cursor);
        }
        return buckets;
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate, RevenueGroup group) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngay bat dau khong duoc sau ngay ket thuc");
        }

        long maxDays = switch (group) {
            case DAY -> 370;
            case MONTH -> 3660;
            case YEAR -> 36500;
        };

        if (fromDate.plusDays(maxDays).isBefore(toDate)) {
            throw new IllegalArgumentException("Khoang thoi gian qua lon cho groupBy=" + group.value);
        }
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private enum RevenueGroup {
        DAY("day"),
        MONTH("month"),
        YEAR("year");

        private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ISO_LOCAL_DATE;
        private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM", Locale.US);
        private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US);
        private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy", Locale.US);
        private static final DateTimeFormatter YEAR_KEY = DateTimeFormatter.ofPattern("yyyy", Locale.US);

        private final String value;

        RevenueGroup(String value) {
            this.value = value;
        }

        static RevenueGroup from(String value) {
            if (value == null || value.isBlank()) {
                return DAY;
            }
            for (RevenueGroup group : values()) {
                if (group.value.equalsIgnoreCase(value)) {
                    return group;
                }
            }
            throw new IllegalArgumentException("groupBy chi ho tro: day, month, year");
        }

        String key(LocalDate date) {
            return switch (this) {
                case DAY -> DAY_KEY.format(date);
                case MONTH -> MONTH_KEY.format(date);
                case YEAR -> YEAR_KEY.format(date);
            };
        }

        String label(LocalDate date) {
            return switch (this) {
                case DAY -> DAY_LABEL.format(date);
                case MONTH -> MONTH_LABEL.format(date);
                case YEAR -> YEAR_KEY.format(date);
            };
        }

        LocalDate periodStart(LocalDate date) {
            return switch (this) {
                case DAY -> date;
                case MONTH -> date.withDayOfMonth(1);
                case YEAR -> date.withDayOfYear(1);
            };
        }

        LocalDate next(LocalDate date) {
            return switch (this) {
                case DAY -> date.plusDays(1);
                case MONTH -> date.plusMonths(1).withDayOfMonth(1);
                case YEAR -> date.plusYears(1).withDayOfYear(1);
            };
        }
    }

    private static class SummaryAccumulator {
        protected BigDecimal totalRevenue = BigDecimal.ZERO;
        protected BigDecimal rentalRevenue = BigDecimal.ZERO;
        protected BigDecimal depositCollected = BigDecimal.ZERO;
        protected BigDecimal depositHeld = BigDecimal.ZERO;
        protected BigDecimal warrantyRevenue = BigDecimal.ZERO;
        protected long orderCount;
        protected long completedOrderCount;
        protected long activeDepositOrderCount;

        void add(RentalOrder order) {
            totalRevenue = totalRevenue.add(safe(order.getGrandTotal()));
            rentalRevenue = rentalRevenue.add(safe(order.getRentalTotal()));
            depositCollected = depositCollected.add(safe(order.getDepositTotal()));
            warrantyRevenue = warrantyRevenue.add(safe(order.getWarrantyTotal()));
            orderCount++;

            if (order.getStatus() == OrderStatus.COMPLETED) {
                completedOrderCount++;
            }
            if (ACTIVE_DEPOSIT_STATUSES.contains(order.getStatus())) {
                depositHeld = depositHeld.add(safe(order.getDepositTotal()));
                activeDepositOrderCount++;
            }
        }

        BigDecimal averageOrderValue() {
            if (orderCount == 0) {
                return BigDecimal.ZERO;
            }
            return totalRevenue.divide(BigDecimal.valueOf(orderCount), 0, RoundingMode.HALF_UP);
        }
    }

    private static class Bucket extends SummaryAccumulator {
        private final String key;
        private final String label;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;

        Bucket(String key, String label, LocalDate periodStart, LocalDate periodEnd) {
            this.key = key;
            this.label = label;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }

        RevenueTimelineResponse.Bar toBar() {
            return RevenueTimelineResponse.Bar.builder()
                    .key(key)
                    .label(label)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .totalRevenue(totalRevenue)
                    .rentalRevenue(rentalRevenue)
                    .depositCollected(depositCollected)
                    .warrantyRevenue(warrantyRevenue)
                    .orderCount(orderCount)
                    .completedOrderCount(completedOrderCount)
                    .build();
        }
    }

    private static class PaymentBucket {
        private final String method;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private long orderCount;

        PaymentBucket(String method) {
            this.method = method;
        }

        void add(RentalOrder order) {
            totalRevenue = totalRevenue.add(safe(order.getGrandTotal()));
            orderCount++;
        }

        BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        RevenueTimelineResponse.PaymentMethodBreakdown toResponse(BigDecimal grandTotal) {
            double percentage = grandTotal.compareTo(BigDecimal.ZERO) == 0
                    ? 0
                    : totalRevenue.multiply(BigDecimal.valueOf(100))
                    .divide(grandTotal, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            return RevenueTimelineResponse.PaymentMethodBreakdown.builder()
                    .method(method)
                    .totalRevenue(totalRevenue)
                    .orderCount(orderCount)
                    .percentage(percentage)
                    .build();
        }
    }
}
