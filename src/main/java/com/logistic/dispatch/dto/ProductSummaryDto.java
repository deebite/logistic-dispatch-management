package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSummaryDto {

    private String productCode;

    private Long totalClosedBatches;
    private Long totalClosedPallets;
    private Long totalSerialsProduced;

    private Long openBatches;
    private Long openPallets;
}
