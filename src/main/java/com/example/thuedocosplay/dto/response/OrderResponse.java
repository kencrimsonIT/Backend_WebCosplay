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
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal rentalTotal;
    private BigDecimal warrantyTotal;
    private BigDecimal depositTotal;
    private BigDecimal discountTotal;
    private String voucherCode;
    private String voucherTitle;
    private BigDecimal grandTotal;
    private LocalDate rentFrom;
    private LocalDate rentTo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
