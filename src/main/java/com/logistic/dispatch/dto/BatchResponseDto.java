package com.logistic.dispatch.dto;

import lombok.Data;

@Data
public class BatchResponseDto {

    private Long batchId;
    private String batchSerialNumber;
    private Long productId;
    private Integer currentUnits;
    private Integer unitsPerBatch;
    private String status;
}
