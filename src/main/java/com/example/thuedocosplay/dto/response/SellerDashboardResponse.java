package com.example.thuedocosplay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SellerDashboardResponse {
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;

    private long productCount;
    private long visibleProductCount;
    private long hiddenProductCount;
    private long availableProductCount;
    private long maintenanceProductCount;
    private long soldProductCount;

    private long totalOrderCount;
    private long pendingOrderCount;
    private long confirmedOrderCount;
    private long rentingOrderCount;
    private long completedOrderCount;
    private long cancelledOrderCount;
    private long returnDueOrderCount;

    private BigDecimal rentalRevenue;
    private BigDecimal depositCollected;
    private BigDecimal depositHeld;
    private BigDecimal estimatedReceivable;
    private BigDecimal averageOrderValue;

    private List<SellerOrderResponse> recentOrders;
    private List<SellerProductResponse> lowStockProducts;
}
