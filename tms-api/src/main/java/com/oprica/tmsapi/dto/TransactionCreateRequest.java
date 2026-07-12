package com.oprica.tmsapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull
        LocalDate transactionDate,

        @NotBlank
        @Pattern(
                regexp = "\\d{4}-\\d{4}-\\d{4}",
                message = "Account number must match ####-####-####"
        )
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
