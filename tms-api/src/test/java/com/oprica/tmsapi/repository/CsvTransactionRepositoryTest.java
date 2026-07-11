package com.oprica.tmsapi.repository;

import com.oprica.tmsapi.exception.InvalidTransactionCsvException;
import com.oprica.tmsapi.exception.TransactionRepositoryException;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.model.TransactionStatus;
import org.apache.commons.csv.CSVException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTransactionRepositoryTest {

    private static final String HEADER = "Transaction Date,Account Number,Account Holder Name,Amount,Status";

    @TempDir
    Path tempDir;

    @Test
    void findAll_whenCsvIsValid_returnsParsedTransactionsInFileOrder() throws IOException {
        TransactionRepository repository = repositoryWithCsv("""
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                2025-03-02,1122-3456-7890,John Smith,75.50,Pending
                2025-03-04,8899-0011-2233,Sarah Williams,310.75,Failed
                """);

        List<Transaction> transactions = repository.findAll();

        assertThat(transactions)
                .containsExactly(
                        new Transaction(
                                LocalDate.of(2025, 3, 1),
                                "7289-3445-1121",
                                "Maria Johnson",
                                new BigDecimal("150.00"),
                                TransactionStatus.SETTLED
                        ),
                        new Transaction(
                                LocalDate.of(2025, 3, 2),
                                "1122-3456-7890",
                                "John Smith",
                                new BigDecimal("75.50"),
                                TransactionStatus.PENDING
                        ),
                        new Transaction(
                                LocalDate.of(2025, 3, 4),
                                "8899-0011-2233",
                                "Sarah Williams",
                                new BigDecimal("310.75"),
                                TransactionStatus.FAILED
                        )
                );
    }

    @Test
    void findAll_whenOnlyHeaderExists_returnsEmptyList() throws IOException {
        TransactionRepository repository = repositoryWithCsv(HEADER + "\n");

        List<Transaction> transactions = repository.findAll();

        assertThat(transactions).isEmpty();
    }

    @Test
    void findAll_whenFileIsEmpty_throwsInvalidTransactionCsvException() throws IOException {
        TransactionRepository repository = repositoryWithCsv("");

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("Invalid CSV headers");
    }

    @Test
    void findAll_whenQuotedFieldContainsComma_parsesCompleteField() throws IOException {
        TransactionRepository repository =
                repositoryWithCsv(csvWithRow("2025-03-01,7289-3445-1121,\"Johnson, Maria\",150.00,Settled"));

        List<Transaction> transactions = repository.findAll();

        assertThat(transactions)
                .containsExactly(
                        new Transaction(
                                LocalDate.of(2025, 3, 1),
                                "7289-3445-1121",
                                "Johnson, Maria",
                                new BigDecimal("150.00"),
                                TransactionStatus.SETTLED
                        )
                );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHeaders")
    void findAll_whenHeaderDoesNotMatchSchema_throwsInvalidTransactionCsvException(String description, String header) throws IOException {
        TransactionRepository repository = repositoryWithCsv(header + "\n");

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("Invalid CSV headers")
                .hasMessageContaining("Expected")
                .hasMessageContaining("found");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRecordWidths")
    void findAll_whenRecordHasIncorrectColumnCount_throwsInvalidTransactionCsvException(String description, String row, int actualColumnCount) throws IOException {
        TransactionRepository repository = repositoryWithCsv(csvWithRow(row));

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining(
                        "has %d columns; expected 5"
                                .formatted(actualColumnCount)
                );
    }

    @Test
    void findAll_whenCsvContainsBlankRecord_rejectsBlankRecord() throws IOException {
        String csv = HEADER + "\n"
                + "2025-03-01,7289-3445-1121,"
                + "Maria Johnson,150.00,Settled\n"
                + "\n"
                + "2025-03-02,1122-3456-7890,"
                + "John Smith,75.50,Pending\n";

        TransactionRepository repository = repositoryWithCsv(csv);

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("columns; expected 5");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingRequiredValues")
    void findAll_whenRequiredValueIsMissing_throwsInvalidTransactionCsvException(String description, String row, String column) throws IOException {
        TransactionRepository repository = repositoryWithCsv(csvWithRow(row));

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("column '%s'".formatted(column))
                .hasMessageContaining("Value is required");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTypedValues")
    void findAll_whenValueCannotBeConverted_throwsInvalidTransactionCsvException(String description, String row, String column, String expectedMessage) throws IOException {
        TransactionRepository repository = repositoryWithCsv(csvWithRow(row));

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("column '%s'".formatted(column))
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void findAll_whenFileDoesNotExist_throwsTransactionRepositoryException() {
        Path missingPath = tempDir.resolve("missing.csv");

        TransactionRepository repository = new CsvTransactionRepository(missingPath);

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(TransactionRepositoryException.class)
                .hasMessageContaining(
                        "Failed to read transactions from " + missingPath
                )
                .hasCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    void findAll_whenHeaderCsvSyntaxIsMalformed_wrapsParsingFailure() throws IOException {
        String malformedCsv = "Transaction Date,Account Number,\"Account Holder Name,Amount,Status";

        TransactionRepository repository = repositoryWithCsv(malformedCsv);

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("Malformed transaction CSV")
                .hasCauseInstanceOf(CSVException.class);
    }

    @Test
    void findAll_whenDataRecordCsvSyntaxIsMalformed_wrapsParsingFailure() throws IOException {
        String malformedCsv = HEADER + "\n"
                + "2025-03-01,7289-3445-1121,"
                + "\"Maria Johnson,150.00,Settled";

        TransactionRepository repository =
                repositoryWithCsv(malformedCsv);

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("Malformed transaction CSV")
                .hasCauseInstanceOf(CSVException.class);
    }

    private static Stream<Arguments> invalidHeaders() {
        return Stream.of(
                Arguments.of(
                        "header name has incorrect case",
                        "transaction date,Account Number,Account Holder Name,Amount,Status"
                ),
                Arguments.of(
                        "headers are in the wrong order",
                        "Account Number,Transaction Date,Account Holder Name,Amount,Status"
                ),
                Arguments.of(
                        "required header is missing",
                        "Transaction Date,Account Number,Account Holder Name,Amount"
                ),
                Arguments.of(
                        "unexpected header is present",
                        HEADER + ",Reference"
                ),
                Arguments.of(
                        "header name is blank",
                        "Transaction Date,Account Number,,Amount,Status"
                ),
                Arguments.of(
                        "header name is duplicated",
                        "Transaction Date,Account Number,Account Number,Amount,Status"
                )
        );
    }

    private static Stream<Arguments> invalidRecordWidths() {
        return Stream.of(
                Arguments.of(
                        "record has too few columns",
                        "2025-03-01,7289-3445-1121,Maria Johnson,150.00",
                        4
                ),
                Arguments.of(
                        "record has too many columns",
                        "2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled,unexpected",
                        6
                )
        );
    }

    private static Stream<Arguments> missingRequiredValues() {
        return Stream.of(
                Arguments.of(
                        "transaction date is missing",
                        ",7289-3445-1121,Maria Johnson,150.00,Settled",
                        "Transaction Date"
                ),
                Arguments.of(
                        "account number is missing",
                        "2025-03-01,,Maria Johnson,150.00,Settled",
                        "Account Number"
                ),
                Arguments.of(
                        "account holder name is missing",
                        "2025-03-01,7289-3445-1121,,150.00,Settled",
                        "Account Holder Name"
                ),
                Arguments.of(
                        "amount is missing",
                        "2025-03-01,7289-3445-1121,Maria Johnson,,Settled",
                        "Amount"
                ),
                Arguments.of(
                        "status is missing",
                        "2025-03-01,7289-3445-1121,Maria Johnson,150.00,",
                        "Status"
                ),
                Arguments.of(
                        "account number contains only whitespace",
                        "2025-03-01,   ,Maria Johnson,150.00,Settled",
                        "Account Number"
                )
        );
    }

    private static Stream<Arguments> invalidTypedValues() {
        return Stream.of(
                Arguments.of(
                        "date has an unsupported format",
                        "01-03-2025,7289-3445-1121,Maria Johnson,150.00,Settled",
                        "Transaction Date",
                        "Expected yyyy-MM-dd"
                ),
                Arguments.of(
                        "amount is not decimal",
                        "2025-03-01,7289-3445-1121,Maria Johnson,one hundred,Settled",
                        "Amount",
                        "Expected a decimal number"
                ),
                Arguments.of(
                        "status is unsupported",
                        "2025-03-01,7289-3445-1121,Maria Johnson,150.00,Completed",
                        "Status",
                        "Unsupported status"
                ),
                Arguments.of(
                        "status has incorrect case",
                        "2025-03-01,7289-3445-1121,Maria Johnson,150.00,settled",
                        "Status",
                        "Unsupported status"
                )
        );
    }

    private TransactionRepository repositoryWithCsv(String csv) throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");
        Files.writeString(csvPath, csv, StandardCharsets.UTF_8);
        return new CsvTransactionRepository(csvPath);
    }

    private static String csvWithRow(String row) {
        return HEADER + "\n" + row + "\n";
    }
}
