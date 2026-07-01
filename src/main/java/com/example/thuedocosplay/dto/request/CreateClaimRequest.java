package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateClaimRequest {

    @NotNull(message = "orderId không được để trống")
    private Long orderId;

    @NotNull(message = "planId không được để trống")
    private Long planId;

    @NotBlank(message = "description không được để trống")
    private String description;

    private String evidenceUrls;  // URLs cách nhau bởi dấu phẩy

    @NotNull @Min(0)
    private BigDecimal requestedAmount;
}
