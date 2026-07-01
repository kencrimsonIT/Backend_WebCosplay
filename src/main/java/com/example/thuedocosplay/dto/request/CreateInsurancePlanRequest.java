package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class CreateInsurancePlanRequest {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    private String description;

    @NotNull @Min(1000)
    private BigDecimal feeAmount;

    @NotNull @Min(1) @Max(100)
    private Integer coverPercent;

    private BigDecimal maxPayout;
}