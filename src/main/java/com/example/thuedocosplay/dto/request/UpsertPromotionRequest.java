package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.PromotionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpsertPromotionRequest {

    @NotBlank(message = "Tên chương trình không được trống")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Mã voucher không được trống")
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "Mã chỉ gồm chữ hoa, số và dấu gạch dưới")
    private String code;

    private PromotionType type = PromotionType.PERCENT;

    @NotNull(message = "Giá trị giảm không được trống")
    @DecimalMin(value = "0.01", message = "Giá trị phải > 0")
    private BigDecimal value;

    @DecimalMin(value = "0")
    private BigDecimal minOrderAmount;

    @Min(value = 1, message = "Giới hạn lượt dùng phải >= 1")
    private Integer maxUses;

    private LocalDate startDate;

    private LocalDate endDate;

    private String targetAudience = "all";

    private String applyTo = "all-costumes";

    private String extraCondition;
}
