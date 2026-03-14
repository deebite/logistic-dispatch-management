package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanResponseDto {

    private String message;

    private String productCode;

    private String productSerialNumber;

    private String batchSerialNumber;

    private Integer currentUnits;

    private Integer maxUnits;

    private String status;
}