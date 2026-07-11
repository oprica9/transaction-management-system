package com.oprica.tmsapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        LocalDate transactionDate,
        String accountNumber,
        String accountHolderName,
        BigDecimal amount,
        TransactionStatus status
) {

}
