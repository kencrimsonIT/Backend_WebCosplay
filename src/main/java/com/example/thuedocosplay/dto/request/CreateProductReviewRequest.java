package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductReviewRequest {
    @NotNull(message = "Vui long chon don hang can danh gia")
    private Long orderId;

    @NotNull(message = "Vui long chon san pham can danh gia")
    private Long productId;

    @NotNull(message = "Vui long chon so sao")
    @Min(value = 1, message = "Danh gia toi thieu 1 sao")
    @Max(value = 5, message = "Danh gia toi da 5 sao")
    private Integer rating;

    @NotBlank(message = "Vui long nhap noi dung danh gia")
    private String content;

    private List<String> imageUrls;
}
