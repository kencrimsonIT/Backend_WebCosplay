package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.OrderStatus;
import com.example.thuedocosplay.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SellerOrderResponse {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private LocalDate rentFrom;
    private LocalDate rentTo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    private BigDecimal orderRentalTotal;
    private BigDecimal orderWarrantyTotal;
    private BigDecimal orderDepositTotal;
    private BigDecimal orderGrandTotal;

    private BigDecimal sellerRentalTotal;
    private BigDecimal sellerDepositTotal;
    private BigDecimal sellerEstimatedReceivable;
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private String categoryName;
        private String productImageUrl;
        private String size;
        private Integer days;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private BigDecimal depositPerItem;
        private BigDecimal depositTotal;
    }
}
