package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QrBatchResult {

    private String batchSerialNumber;
    private boolean success;
    private String message;

    public String getBatchSerialNumber() { return batchSerialNumber; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

