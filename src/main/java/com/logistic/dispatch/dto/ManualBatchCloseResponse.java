package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManualBatchCloseResponse {

    private String message;
    private String batchSerialNumber;
    private String qrImage;
}
