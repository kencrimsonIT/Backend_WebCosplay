package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
}
