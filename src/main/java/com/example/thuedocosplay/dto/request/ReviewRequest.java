package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "orderId không được để trống")
    private Long orderId;      // Đơn hàng gốc (để xác minh đã thuê)

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull @Min(1) @Max(5)
    private Integer rating;

    private String comment;    // Optional
}
