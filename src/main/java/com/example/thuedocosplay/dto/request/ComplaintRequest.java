package com.example.thuedocosplay.dto.request;

import com.example.thuedocosplay.entity.enums.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @NotBlank(message = "Description is required")
    private String description;
}
