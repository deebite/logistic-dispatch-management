package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PalletBatchesResponseDto {

    private String palletSerialNumber;

    private String productCode;

    private Integer currentBatches;

    private Integer maxBatches;

    private List<String> batchSerialNumbers;
}
