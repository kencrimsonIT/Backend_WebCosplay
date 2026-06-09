package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.PaymentMethod;
import com.example.thuedocosplay.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private String txnRef;
    private Long orderId;
    private String orderCode;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String paymentUrl;
}
