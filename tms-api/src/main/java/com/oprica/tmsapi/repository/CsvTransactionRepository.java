package com.oprica.tmsapi.repository;

import com.oprica.tmsapi.csv.TransactionCsvColumn;
import com.oprica.tmsapi.exception.InvalidTransactionCsvException;
import com.oprica.tmsapi.exception.TransactionRepositoryException;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.model.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.oprica.tmsapi.csv.TransactionCsvColumn.ACCOUNT_HOLDER_NAME;
import static com.oprica.tmsapi.csv.TransactionCsvColumn.ACCOUNT_NUMBER;
import static com.oprica.tmsapi.csv.TransactionCsvColumn.AMOUNT;
import static com.oprica.tmsapi.csv.TransactionCsvColumn.STATUS;
import static com.oprica.tmsapi.csv.TransactionCsvColumn.TRANSACTION_DATE;

@RequiredArgsConstructor
public class CsvTransactionRepository implements TransactionRepository {

    /*
     * Header validation is performed explicitly against TransactionCsvColumn.
     * Commons CSV is allowed to parse duplicate or missing header names so that
     * every header mismatch results in InvalidTransactionCsvException.
     */
    private static final CSVFormat READ_FORMAT =
            CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setAllowMissingColumnNames(true)
                    .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL)
                    .get();

    private static final CSVFormat WRITE_FORMAT = CSVFormat.RFC4180;

    private final Path path;

    @Override
    public List<Transaction> findAll() {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = READ_FORMAT.parse(reader)) {

            validateHeaders(parser);
            return parseTransactions(parser);

        } catch (UncheckedIOException exception) {
            throw mapReadException(exception.getCause());
        } catch (IOException exception) {
            throw mapReadException(exception);
        }
    }

    @Override
    public Transaction save(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");

        try {
            appendTransaction(transaction);
            return transaction;
        } catch (IOException exception) {
            throw new TransactionRepositoryException("Failed to save transaction to " + path, exception);
        }
    }

    private void appendTransaction(Transaction transaction) throws IOException {
        boolean newFile = Files.notExists(path) || Files.size(path) == 0;
        boolean needsRecordSeparator = !newFile && !endsWithLineBreak(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
             CSVPrinter printer = new CSVPrinter(writer, WRITE_FORMAT)) {

            if (needsRecordSeparator) {
                printer.println();
            }

            if (newFile) {
                printer.printRecord(TransactionCsvColumn.headers());
            }

            printer.printRecord(
                    transaction.transactionDate(),
                    transaction.accountNumber(),
                    transaction.accountHolderName(),
                    transaction.amount().toPlainString(),
                    transaction.status().getValue()
            );
        }
    }

    private RuntimeException mapReadException(IOException exception) {
        if (exception instanceof CSVException) {
            return new InvalidTransactionCsvException("Malformed transaction CSV", exception);
        }

        return new TransactionRepositoryException("Failed to read transactions from " + path, exception);
    }

    private static List<Transaction> parseTransactions(CSVParser parser) {
        List<Transaction> transactions = new ArrayList<>();

        for (CSVRecord record : parser) {
            validateColumnCount(record);
            transactions.add(parseTransaction(record));
        }

        return transactions;
    }

    private static Transaction parseTransaction(CSVRecord record) {
        try {
            return new Transaction(
                    parseTransactionDate(record),
                    required(record, ACCOUNT_NUMBER),
                    required(record, ACCOUNT_HOLDER_NAME),
                    parseAmount(record),
                    parseStatus(record)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTransactionCsvException("Record %d contains invalid transaction data: %s"
                    .formatted(record.getRecordNumber(), exception.getMessage()), exception);
        }
    }

    private static LocalDate parseTransactionDate(CSVRecord record) {
        String value = required(record, TRANSACTION_DATE);

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw invalid(record, TRANSACTION_DATE, "Expected yyyy-MM-dd but found '%s'".formatted(value), exception);
        }
    }

    private static BigDecimal parseAmount(CSVRecord record) {
        String value = required(record, AMOUNT);

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw invalid(record, AMOUNT, "Expected a decimal number but found '%s'".formatted(value), exception);
        }
    }

    private static TransactionStatus parseStatus(CSVRecord record) {
        String value = required(record, STATUS);

        try {
            return TransactionStatus.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw invalid(record, STATUS, "Unsupported status '%s'".formatted(value), exception);
        }
    }

    private static String required(CSVRecord record, TransactionCsvColumn column) {
        String value = record.get(column.getHeader());

        if (value == null || value.isBlank()) {
            throw new InvalidTransactionCsvException(errorMessage(record, column, "Value is required"));
        }

        return value;
    }

    private static void validateHeaders(CSVParser parser) {
        List<String> expected = TransactionCsvColumn.headers();
        List<String> actual = parser.getHeaderNames();

        if (!actual.equals(expected)) {
            throw new InvalidTransactionCsvException("Invalid CSV headers. Expected %s but found %s".formatted(expected, actual));
        }
    }

    private static void validateColumnCount(CSVRecord record) {
        int expected = TransactionCsvColumn.values().length;

        if (record.size() != expected) {
            throw new InvalidTransactionCsvException("Record %d has %d columns; expected %d".formatted(record.getRecordNumber(), record.size(), expected));
        }
    }

    private static boolean endsWithLineBreak(Path path) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            long size = channel.size();

            if (size == 0) {
                return true;
            }

            ByteBuffer buffer = ByteBuffer.allocate(1);
            channel.position(size - 1);
            channel.read(buffer);
            buffer.flip();

            byte lastByte = buffer.get();

            return lastByte == '\n' || lastByte == '\r';
        }
    }

    private static InvalidTransactionCsvException invalid(CSVRecord record, TransactionCsvColumn column, String message, Throwable cause) {
        return new InvalidTransactionCsvException(errorMessage(record, column, message), cause);
    }

    private static String errorMessage(CSVRecord record, TransactionCsvColumn column, String message) {
        return "Record %d, column '%s': %s".formatted(record.getRecordNumber(), column.getHeader(), message);
    }
}
