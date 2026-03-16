package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkScanResponseDto {

    private String batchSerialNumber;
    private int processedCount;
    private List<SerialProcessResult> results;
    private String batchStatus;
    private int remainingUnits;
    private String qrImage;
}

