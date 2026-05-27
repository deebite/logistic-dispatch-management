package com.logistic.dispatch.dto;

import com.logistic.dispatch.utility.LifeCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchSummaryResponseDto {

    private String batchSerialNumber;

    private Integer currentUnits;

    private Integer maxUnits;

    private LifeCycleStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;
}