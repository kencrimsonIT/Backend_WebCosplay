package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.OrderItemResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.entity.OrderItem;
import com.example.thuedocosplay.entity.Product;
import com.example.thuedocosplay.entity.RentalOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(RentalOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .rentalTotal(order.getRentalTotal())
                .warrantyTotal(order.getWarrantyTotal())
                .depositTotal(order.getDepositTotal())
                .discountTotal(order.getDiscountTotal())
                .voucherCode(order.getVoucherCode())
                .voucherTitle(order.getVoucherTitle())
                .grandTotal(order.getGrandTotal())
                .rentFrom(order.getRentFrom())
                .rentTo(order.getRentTo())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(OrderMapper::toItemResponse).toList())
                .build();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        Product product = item.getProduct();
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(item.getProductName())
                .categoryName(item.getCategoryName())
                .productImageUrl(product != null ? product.getImageUrl() : null)
                .size(item.getSize())
                .days(item.getDays())
                .quantity(item.getQuantity())
                .unitPrice(calculateUnitPrice(item))
                .lineTotal(item.getLineTotal())
                .build();
    }

    private static BigDecimal calculateUnitPrice(OrderItem item) {
        if (item.getLineTotal() == null || item.getDays() == null || item.getQuantity() == null
                || item.getDays() <= 0 || item.getQuantity() <= 0) {
            return BigDecimal.ZERO;
        }
        return item.getLineTotal()
                .divide(BigDecimal.valueOf((long) item.getDays() * item.getQuantity()), 0, RoundingMode.HALF_UP);
    }

    public static List<OrderResponse> toResponses(List<RentalOrder> orders) {
        return orders.stream().map(OrderMapper::toResponse).toList();
    }
}
