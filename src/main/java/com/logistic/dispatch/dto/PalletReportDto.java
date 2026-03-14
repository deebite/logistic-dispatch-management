package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PalletReportDto {

    private String palletSerialNumber;
    private long batchesInPallet;
    private String qrStatus;
}
