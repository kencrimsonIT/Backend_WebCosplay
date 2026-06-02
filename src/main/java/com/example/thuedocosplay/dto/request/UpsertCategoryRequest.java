package com.example.thuedocosplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertCategoryRequest {
    @NotBlank
    private String name;

    private Boolean active;
}
