package com.oprica.tmsapi.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull
        @PastOrPresent
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
        @Digits(integer = 15, fraction = 2)
        BigDecimal amount
) {

}
