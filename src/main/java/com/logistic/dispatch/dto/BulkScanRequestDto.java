package com.logistic.dispatch.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkScanRequestDto {
    private String productCode;
    private List<String> serialNumbers;
}

