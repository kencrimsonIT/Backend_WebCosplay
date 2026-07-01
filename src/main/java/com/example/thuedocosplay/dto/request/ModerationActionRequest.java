package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.ModerationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModerationActionRequest {

    @NotNull(message = "status không được để trống")
    private ModerationStatus status;   // APPROVED | HIDDEN

    private String note;               // Lý do (bắt buộc khi HIDDEN, optional khi APPROVED)
}
