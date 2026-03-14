package com.logistic.dispatch.exception;

public class OptimisticLockException extends RuntimeException{
    public OptimisticLockException(String message){
        super(message);
    }
}
