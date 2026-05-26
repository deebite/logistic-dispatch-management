package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PalletQrResponseDto {

    private String palletSerialNumber;

    private String qrImage;
}