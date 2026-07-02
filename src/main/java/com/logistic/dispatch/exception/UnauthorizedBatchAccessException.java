package com.logistic.dispatch.exception;

public class UnauthorizedBatchAccessException extends RuntimeException {
    public UnauthorizedBatchAccessException(String message) {
        super(message);
    }
}
