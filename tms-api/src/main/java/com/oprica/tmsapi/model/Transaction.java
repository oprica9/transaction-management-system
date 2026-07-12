package com.oprica.tmsapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable transaction domain model.
 *
 * <p>Account identifiers and holder names are stripped of surrounding
 * whitespace. All fields are required and the amount must be greater than zero.
 */
public record Transaction(
        LocalDate transactionDate,
        String accountNumber,
        String accountHolderName,
        BigDecimal amount,
        TransactionStatus status
) {

    public Transaction {
        requireNonNull(transactionDate, "transactionDate");
        requireNonNull(amount, "amount");
        requireNonNull(status, "status");

        accountNumber = normalizeRequired(accountNumber, "accountNumber");
        accountHolderName = normalizeRequired(accountHolderName, "accountHolderName");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.strip();
    }
}
