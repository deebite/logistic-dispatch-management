package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchDetailDto {

    private String batchSerialNumber;

    private String batchStatus;

    private Integer currentUnits;

    private Integer maxUnits;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private String assignedUser;

    private List<String> productSerialNumbers;

    private PalletInfoDto pallet;
}
