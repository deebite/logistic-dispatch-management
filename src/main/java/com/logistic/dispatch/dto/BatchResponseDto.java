package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchResponseDto {

    private String batchSerialNumber;

    private String assignedUser;

    private String status;

    private String message;
}
