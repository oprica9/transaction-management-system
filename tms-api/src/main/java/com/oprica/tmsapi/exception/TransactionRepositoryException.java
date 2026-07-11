package com.oprica.tmsapi.exception;

public class TransactionRepositoryException extends RuntimeException {

    public TransactionRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}