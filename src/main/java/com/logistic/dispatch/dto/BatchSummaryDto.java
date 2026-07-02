package com.logistic.dispatch.dto;

import com.logistic.dispatch.utility.BatchAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSummaryDto {

    private String productCode;

    private String batchSerialNumber;

    private Integer currentUnits;

    private Integer maxUnits;

    private String status;

    private String assignedUser;

    private LocalDateTime assignedAt;

    private BatchAction action;
}