package com.logistic.dispatch.exception;

public class SelfDeactivationNotAllowedException extends RuntimeException{
    public SelfDeactivationNotAllowedException(String message) {
        super(message);
    }
}
