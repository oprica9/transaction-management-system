package com.oprica.tmsapi.csv;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum TransactionCsvColumn {

    TRANSACTION_DATE("Transaction Date"),
    ACCOUNT_NUMBER("Account Number"),
    ACCOUNT_HOLDER_NAME("Account Holder Name"),
    AMOUNT("Amount"),
    STATUS("Status");

    private static final List<String> HEADERS = Arrays.stream(values())
            .map(TransactionCsvColumn::getHeader)
            .toList();

    private final String header;

    public static List<String> headers() {
        return HEADERS;
    }
}
