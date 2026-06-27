package com.logistic.dispatch.dto;

import com.logistic.dispatch.utility.GrtStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GrtReportRequestDto {

    private LocalDateTime dateTime;

    @NotBlank(message = "Serial Number is required")
    private String serialNo;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Machine is required")
    private String machine;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Operator is required")
    private String operator;

    @NotBlank(message = "Shift is required")
    private String shift;

    private BigDecimal d1;
    private BigDecimal d2;
    private BigDecimal d3;
    private BigDecimal d4;
    private BigDecimal d5;
    private BigDecimal d6;
    private BigDecimal d7;
    private BigDecimal d8;
    private BigDecimal d9;
    private BigDecimal d10;

    private BigDecimal d11;
    private BigDecimal d12;
    private BigDecimal d13;
    private BigDecimal d14;
    private BigDecimal d15;
    private BigDecimal d16;
    private BigDecimal d17;
    private BigDecimal d18;
    private BigDecimal d19;
    private BigDecimal d20;

    private BigDecimal d21;
    private BigDecimal d22;
    private BigDecimal d23;
    private BigDecimal d24;
    private BigDecimal d25;
    private BigDecimal d26;
    private BigDecimal d27;
    private BigDecimal d28;
    private BigDecimal d29;
    private BigDecimal d30;

    private BigDecimal d31;
    private BigDecimal d32;
    private BigDecimal d33;
    private BigDecimal d34;
    private BigDecimal d35;
    private BigDecimal d36;
    private BigDecimal d37;
    private BigDecimal d38;
    private BigDecimal d39;
    private BigDecimal d40;

    @NotNull(message = "Station Number is required")
    @Positive(message = "Station Number must be positive")
    private Integer stationNo;
}