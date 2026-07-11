package com.oprica.tmsapi.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum TransactionStatus {
    PENDING("Pending"),
    SETTLED("Settled"),
    FAILED("Failed");

    private final String value;

    public static TransactionStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported transaction status: " + value));
    }
}
