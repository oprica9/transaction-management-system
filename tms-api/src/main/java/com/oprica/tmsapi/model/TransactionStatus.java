package com.oprica.tmsapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum TransactionStatus {

    PENDING("Pending"),
    SETTLED("Settled"),
    FAILED("Failed");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Resolves a status from its external representation.
     *
     * <p>Matching is case-sensitive; for example, {@code "Settled"} is valid while
     * {@code "settled"} is not.
     *
     * @param value external status value
     * @return matching transaction status
     * @throws IllegalArgumentException if the value is unsupported
     */
    @JsonCreator
    public static TransactionStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported transaction status: " + value));
    }
}
