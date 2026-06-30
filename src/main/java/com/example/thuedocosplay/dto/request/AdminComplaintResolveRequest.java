package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminComplaintResolveRequest {
    @NotNull(message = "Status is required")
    private ComplaintStatus status;
    
    private String adminResponse;
}
