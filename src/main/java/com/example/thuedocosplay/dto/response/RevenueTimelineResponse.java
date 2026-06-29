package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RevenueTimelineResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String groupBy;
    private Summary summary;
    private List<Bar> bars;
    private List<PaymentMethodBreakdown> paymentMethods;

    @Data
    @Builder
    public static class Summary {
        private BigDecimal totalRevenue;
        private BigDecimal rentalRevenue;
        private BigDecimal depositCollected;
        private BigDecimal depositHeld;
        private BigDecimal warrantyRevenue;
        private BigDecimal averageOrderValue;
        private long orderCount;
        private long completedOrderCount;
        private long activeDepositOrderCount;
    }

    @Data
    @Builder
    public static class Bar {
        private String key;
        private String label;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal totalRevenue;
        private BigDecimal rentalRevenue;
        private BigDecimal depositCollected;
        private BigDecimal warrantyRevenue;
        private long orderCount;
        private long completedOrderCount;
    }

    @Data
    @Builder
    public static class PaymentMethodBreakdown {
        private String method;
        private BigDecimal totalRevenue;
        private long orderCount;
        private double percentage;
    }
}
