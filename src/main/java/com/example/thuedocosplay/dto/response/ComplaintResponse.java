package com.example.thuedocosplay.dto.response;

import com.example.thuedocosplay.entity.enums.ComplaintStatus;
import com.example.thuedocosplay.entity.enums.ComplaintCategory;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ComplaintResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String title;
    private ComplaintCategory category;
    private String description;
    private String adminResponse;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
