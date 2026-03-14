package com.logistic.dispatch.exception;

public class PalletAssignmentException extends RuntimeException{
    public PalletAssignmentException(String message) {
        super(message);
    }

    PalletAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
