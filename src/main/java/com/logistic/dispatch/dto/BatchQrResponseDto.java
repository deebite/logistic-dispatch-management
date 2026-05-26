package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchQrResponseDto {

    private String batchSerialNumber;

    private String qrImage;
}