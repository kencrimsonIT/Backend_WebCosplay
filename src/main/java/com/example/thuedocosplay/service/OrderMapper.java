package com.example.thuedocosplay.service;

import com.example.thuedocosplay.dto.response.OrderItemResponse;
import com.example.thuedocosplay.dto.response.OrderResponse;
import com.example.thuedocosplay.entity.OrderItem;
import com.example.thuedocosplay.entity.RentalOrder;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(RentalOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .rentalTotal(order.getRentalTotal())
                .warrantyTotal(order.getWarrantyTotal())
                .depositTotal(order.getDepositTotal())
                .grandTotal(order.getGrandTotal())
                .rentFrom(order.getRentFrom())
                .rentTo(order.getRentTo())
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(OrderMapper::toItemResponse).toList())
                .build();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .categoryName(item.getCategoryName())
                .size(item.getSize())
                .days(item.getDays())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }

    public static List<OrderResponse> toResponses(List<RentalOrder> orders) {
        return orders.stream().map(OrderMapper::toResponse).toList();
    }
}
