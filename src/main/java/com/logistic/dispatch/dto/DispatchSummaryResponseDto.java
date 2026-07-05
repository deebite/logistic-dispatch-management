package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchSummaryResponseDto {

    private ProductSummary product;

    private BatchSummary batchSummary;

    private PalletSummary palletSummary;

    private List<DispatchDetailDto> details;
}
