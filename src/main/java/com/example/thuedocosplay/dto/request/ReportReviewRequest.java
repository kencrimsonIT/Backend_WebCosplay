package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportReviewRequest {

    @NotBlank(message = "reason không được để trống")
    private String reason;     // SPAM | OFFENSIVE | FAKE | OTHER

    private String detail;
}
