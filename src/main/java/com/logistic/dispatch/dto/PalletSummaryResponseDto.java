package com.logistic.dispatch.dto;

import com.logistic.dispatch.utility.LifeCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PalletSummaryResponseDto {

    private String palletSerialNumber;

    private Integer currentBatches;

    private Integer maxBatches;

    private LifeCycleStatus status;

    private String productCode;

    private String productName;

    private List<String> batchSerialLists;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;
}