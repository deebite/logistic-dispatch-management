package com.logistic.dispatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScanProductDto {
    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Product serial number is required")
    private String productSerialNumber;
}
