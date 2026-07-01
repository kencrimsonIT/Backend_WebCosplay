package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOnlinePaymentRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private PaymentMethod method;
}
