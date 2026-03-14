package com.logistic.dispatch.dto;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class QrProcessResponse {

    private boolean success;
    private String message;
    private int processedCount;
    private List<QrBatchResult> results;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getProcessedCount() { return processedCount; }
    public List<QrBatchResult> getResults() { return results; }
}

