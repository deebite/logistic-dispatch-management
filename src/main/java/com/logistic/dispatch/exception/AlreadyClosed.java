package com.logistic.dispatch.exception;

public class AlreadyClosed extends RuntimeException{
    public AlreadyClosed(String message) {
        super(message);
    }
}
