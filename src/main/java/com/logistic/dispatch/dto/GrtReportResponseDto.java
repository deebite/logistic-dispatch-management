package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrtReportResponseDto {

    private String serialNo;
    private String model;
    private String machine;
    private String status;
    private String operator;
    private String shift;
    private String message;
}
