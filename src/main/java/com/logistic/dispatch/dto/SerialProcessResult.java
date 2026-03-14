package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SerialProcessResult {
    private String serialNumber;
    private boolean success;
    private String message;
}
