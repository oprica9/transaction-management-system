package com.oprica.tmsapi.exception;

public class InvalidTransactionCsvException extends RuntimeException {

    public InvalidTransactionCsvException(String message) {
        super(message);
    }

    public InvalidTransactionCsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
