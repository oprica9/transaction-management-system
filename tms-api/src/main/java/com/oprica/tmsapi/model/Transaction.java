package com.oprica.tmsapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public record Transaction(
        LocalDate transactionDate,
        String accountNumber,
        String accountHolderName,
        BigDecimal amount,
        TransactionStatus status
) {

    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{4}-\\d{4}-\\d{4}");

    public Transaction {
        requireNonNull(transactionDate, "transactionDate");
        requireNonNull(amount, "amount");
        requireNonNull(status, "status");

        requireNonBlank(accountNumber, "accountNumber");
        requireNonBlank(accountHolderName, "accountHolderName");

        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException("accountNumber must match ####-####-####");
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
