package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchProductsResponseDto {

    private String batchSerialNumber;
    private String productCode;
    private Integer currentUnits;
    private Integer maxUnits;
    private List<String> productSerialNumbers;
}
