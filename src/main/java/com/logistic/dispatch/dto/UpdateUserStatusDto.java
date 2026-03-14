package com.logistic.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserStatusDto {

    @NotBlank(message = "Status is required")
    private String status;
}
