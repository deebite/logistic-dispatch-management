package com.logistic.dispatch.dto;

import com.logistic.dispatch.utility.LifeCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PalletSummaryResponseDto {

    private String palletSerialNumber;

    private Integer currentBatches;

    private Integer maxBatches;

    private LifeCycleStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;
}