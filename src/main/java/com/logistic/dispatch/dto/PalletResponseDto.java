package com.logistic.dispatch.dto;

import lombok.Data;

@Data
public class PalletResponseDto {

    private Long palletId;
    private String palletSerialNumber;
    private Integer currentBatches;
    private Integer maxBatches;
    private String status;
}
