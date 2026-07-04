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
public class BatchSummaryResponseDto {

    private String batchSerialNumber;

    private Integer currentUnits;

    private Integer maxUnits;

    private LifeCycleStatus status;

    private String productCode;

    private String productName;

    private List<String> productSerialNumbers;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;
}