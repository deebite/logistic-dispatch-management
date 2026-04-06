package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualPalletCloseResponse {

    private String message;
    private String palletSerialNumber;
    private String qrImage;
}
