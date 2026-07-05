package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalletInfoDto {

    private String palletSerialNumber;

    private String palletStatus;

    private Integer currentBatches;

    private Integer maxBatches;
}
