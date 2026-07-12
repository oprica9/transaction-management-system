package com.oprica.tmsapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull
        LocalDate transactionDate,

        @NotBlank
        String accountNumber,

        @NotBlank
        String accountHolderName,

        @NotNull
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Amount must be greater than zero"
        )
        BigDecimal amount
) {
}
