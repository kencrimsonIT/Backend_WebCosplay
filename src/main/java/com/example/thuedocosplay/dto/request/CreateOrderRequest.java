package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String customerName;

    private String customerPhone;

    @Email
    private String customerEmail;

    private String shippingAddress;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    private BigDecimal rentalTotal;

    @NotNull
    private BigDecimal warrantyTotal;

    @NotNull
    private BigDecimal depositTotal;

    @NotNull
    private BigDecimal grandTotal;

    private LocalDate rentFrom;
    private LocalDate rentTo;

    private String promotionCode;
    private BigDecimal discountTotal;

    @NotEmpty
    @Valid
    private List<CreateOrderItemRequest> items;
}
