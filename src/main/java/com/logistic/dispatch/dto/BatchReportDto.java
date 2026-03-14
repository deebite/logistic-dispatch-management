package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BatchReportDto {

    private String batchNumber;
    private String productCode;
    private Integer totalUnits;
    private Integer maxUnits;
    private String status;
    private String qrStatus;
    private LocalDateTime closedAt;
}
