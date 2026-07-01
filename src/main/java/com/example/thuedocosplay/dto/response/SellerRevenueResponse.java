package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SellerRevenueResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String groupBy;
    private Summary summary;
    private List<Bar> bars;

    @Data
    @Builder
    public static class Summary {
        private BigDecimal rentalRevenue;
        private BigDecimal depositCollected;
        private BigDecimal depositHeld;
        private BigDecimal estimatedReceivable;
        private BigDecimal averageOrderValue;
        private long orderCount;
        private long completedOrderCount;
    }

    @Data
    @Builder
    public static class Bar {
        private String key;
        private String label;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal rentalRevenue;
        private BigDecimal depositCollected;
        private BigDecimal estimatedReceivable;
        private long orderCount;
        private long completedOrderCount;
    }
}
