package com.oprica.tmsapi.repository;

import com.oprica.tmsapi.exception.InvalidTransactionCsvException;
import com.oprica.tmsapi.exception.TransactionRepositoryException;
import com.oprica.tmsapi.model.Transaction;
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

import static com.oprica.tmsapi.model.TransactionStatus.FAILED;
import static com.oprica.tmsapi.model.TransactionStatus.PENDING;
import static com.oprica.tmsapi.model.TransactionStatus.SETTLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTransactionRepositoryTest {

    private static final String HEADER = "Transaction Date,Account Number,Account Holder Name,Amount,Status";

    private static final Transaction TRANSACTION_TO_SAVE =
            new Transaction(
                    LocalDate.of(2026, 12, 7),
                    "1234-5678-9101",
                    "Test Holder",
                    new BigDecimal("1000.0"),
                    PENDING
            );

    private static final String TRANSACTION_ROW = "2026-12-07,1234-5678-9101,Test Holder,1000.0,Pending";

    @TempDir
    Path tempDir;

    @Test
    void constructor_whenFileAndParentDirectoriesDoNotExist_initializesCsv() throws IOException {
        Path csvPath = tempDir
                .resolve("nested")
                .resolve("data")
                .resolve("transactions.csv");

        CsvTransactionRepository repository = new CsvTransactionRepository(csvPath);

        assertThat(csvPath).isRegularFile();

        assertThat(Files.readAllLines(
                csvPath,
                StandardCharsets.UTF_8
        )).containsExactly(HEADER);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void constructor_whenFileIsEmpty_writesHeader() throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");
        Files.createFile(csvPath);

        CsvTransactionRepository repository = new CsvTransactionRepository(csvPath);

        assertThat(Files.readAllLines(
                csvPath,
                StandardCharsets.UTF_8
        )).containsExactly(HEADER);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void constructor_whenFileExists_doesNotModifyIt() throws IOException {
        String existingCsv = """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                """;

        Path csvPath = tempDir.resolve("transactions.csv");

        Files.writeString(
                csvPath,
                existingCsv,
                StandardCharsets.UTF_8
        );

        new CsvTransactionRepository(csvPath);

        assertThat(Files.readString(
                csvPath,
                StandardCharsets.UTF_8
        )).isEqualTo(existingCsv);
    }

    @Test
    void constructor_whenPathIsNull_throwsNullPointerException() {
        assertThatThrownBy(
                () -> new CsvTransactionRepository(null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("path");
    }

    @Test
    void findAll_whenCsvIsValid_returnsParsedTransactionsInFileOrder()
            throws IOException {
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
                                SETTLED
                        ),
                        new Transaction(
                                LocalDate.of(2025, 3, 2),
                                "1122-3456-7890",
                                "John Smith",
                                new BigDecimal("75.50"),
                                PENDING
                        ),
                        new Transaction(
                                LocalDate.of(2025, 3, 4),
                                "8899-0011-2233",
                                "Sarah Williams",
                                new BigDecimal("310.75"),
                                FAILED
                        )
                );
    }

    @Test
    void findAll_whenOnlyHeaderExists_returnsEmptyList() throws IOException {
        TransactionRepository repository = repositoryWithCsv(HEADER + "\n");
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_whenQuotedFieldContainsComma_parsesCompleteField() throws IOException {
        TransactionRepository repository =
                repositoryWithCsv(csvWithRow("2025-03-01,7289-3445-1121,\"Johnson, Maria\",150.00,Settled"));

        assertThat(repository.findAll())
                .containsExactly(
                        new Transaction(
                                LocalDate.of(2025, 3, 1),
                                "7289-3445-1121",
                                "Johnson, Maria",
                                new BigDecimal("150.00"),
                                SETTLED
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

        TransactionRepository repository = repositoryWithCsv(malformedCsv);

        assertThatThrownBy(repository::findAll)
                .isInstanceOf(InvalidTransactionCsvException.class)
                .hasMessageContaining("Malformed transaction CSV")
                .hasCauseInstanceOf(CSVException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidExistingCsvFiles")
    void validate_whenExistingCsvIsInvalid_throwsWithoutModifyingFile(String description, String existingCsv) throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");

        Files.writeString(csvPath, existingCsv, StandardCharsets.UTF_8);

        String originalContent = Files.readString(csvPath, StandardCharsets.UTF_8);

        CsvTransactionRepository repository = new CsvTransactionRepository(csvPath);

        assertThatThrownBy(repository::validate)
                .isInstanceOf(InvalidTransactionCsvException.class);

        assertThat(Files.readString(csvPath, StandardCharsets.UTF_8))
                .isEqualTo(originalContent);
    }

    @Test
    void save_whenFileContainsOnlyHeader_appendsTransaction() throws IOException {
        TransactionRepository repository = repositoryWithCsv(HEADER + "\n");

        Transaction saved = repository.save(TRANSACTION_TO_SAVE);
        assertThat(saved).isSameAs(TRANSACTION_TO_SAVE);
        assertThat(repository.findAll())
                .containsExactly(TRANSACTION_TO_SAVE);
    }

    @Test
    void save_whenFileDoesNotEndWithLineBreak_startsTransactionOnNewRecord() throws IOException {
        TransactionRepository repository = repositoryWithCsv(HEADER);

        repository.save(TRANSACTION_TO_SAVE);

        assertThat(Files.readAllLines(
                tempDir.resolve("transactions.csv"),
                StandardCharsets.UTF_8
        )).containsExactly(
                HEADER,
                TRANSACTION_ROW
        );
    }

    @Test
    void save_whenFileContainsTransactions_appendsWithoutOverwritingExistingRows() throws IOException {
        String existingCsv = """
                Transaction Date,Account Number,Account Holder Name,Amount,Status
                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                """;

        TransactionRepository repository = repositoryWithCsv(existingCsv);

        repository.save(TRANSACTION_TO_SAVE);

        assertThat(repository.findAll())
                .containsExactly(
                        new Transaction(
                                LocalDate.of(2025, 3, 1),
                                "7289-3445-1121",
                                "Maria Johnson",
                                new BigDecimal("150.00"),
                                SETTLED
                        ),
                        TRANSACTION_TO_SAVE
                );

        assertThat(Files.readAllLines(
                tempDir.resolve("transactions.csv"),
                StandardCharsets.UTF_8
        )).containsExactly(
                HEADER,
                "2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled",
                TRANSACTION_ROW
        );
    }

    @Test
    void save_whenCalledMultipleTimes_writesHeaderOnlyOnce() throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");

        TransactionRepository repository = new CsvTransactionRepository(csvPath);

        Transaction secondTransaction = new Transaction(
                LocalDate.of(2026, 12, 8),
                "2222-3333-4444",
                "Second Holder",
                new BigDecimal("25.50"),
                SETTLED
        );

        repository.save(TRANSACTION_TO_SAVE);
        repository.save(secondTransaction);

        assertThat(repository.findAll())
                .containsExactly(
                        TRANSACTION_TO_SAVE,
                        secondTransaction
                );

        assertThat(Files.readAllLines(csvPath, StandardCharsets.UTF_8))
                .containsExactly(
                        HEADER,
                        TRANSACTION_ROW,
                        "2026-12-08,2222-3333-4444,Second Holder,25.50,Settled"
                );
    }

    @Test
    void save_whenFieldContainsComma_quotesFieldAndPreservesValue() throws IOException {
        Transaction transaction = new Transaction(
                LocalDate.of(2026, 12, 7),
                "1234-5678-9101",
                "Holder, Test",
                new BigDecimal("1000.0"),
                PENDING
        );

        TransactionRepository repository = repositoryWithCsv(HEADER + "\n");

        repository.save(transaction);

        assertThat(repository.findAll())
                .containsExactly(transaction);

        assertThat(Files.readAllLines(
                tempDir.resolve("transactions.csv"),
                StandardCharsets.UTF_8
        )).containsExactly(
                HEADER,
                "2026-12-07,1234-5678-9101,\"Holder, Test\",1000.0,Pending"
        );
    }

    @Test
    void save_whenFileIsDeletedAfterInitialization_throwsTransactionRepositoryException() throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");

        TransactionRepository repository =
                new CsvTransactionRepository(csvPath);

        Files.delete(csvPath);

        assertThatThrownBy(
                () -> repository.save(TRANSACTION_TO_SAVE)
        )
                .isInstanceOf(TransactionRepositoryException.class)
                .hasMessageContaining(
                        "Failed to save transaction to " + csvPath
                )
                .hasCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    void save_whenTransactionIsNull_throwsNullPointerException() throws IOException {
        TransactionRepository repository = repositoryWithCsv(HEADER + "\n");

        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("transaction");
    }

    private static Stream<Arguments> invalidExistingCsvFiles() {
        return Stream.of(
                Arguments.of(
                        "existing file has invalid headers",
                        """
                                Wrong Header,Account Number,Account Holder Name,Amount,Status
                                2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
                                """
                ),
                Arguments.of(
                        "existing file contains invalid amount",
                        """
                                Transaction Date,Account Number,Account Holder Name,Amount,Status
                                2025-03-01,7289-3445-1121,Maria Johnson,invalid,Settled
                                """
                ),
                Arguments.of(
                        "existing file contains malformed CSV",
                        """
                                Transaction Date,Account Number,Account Holder Name,Amount,Status
                                2025-03-01,7289-3445-1121,"Maria Johnson,150.00,Settled
                                """
                )
        );
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

    private CsvTransactionRepository repositoryWithCsv(String csv) throws IOException {
        Path csvPath = tempDir.resolve("transactions.csv");
        Files.writeString(csvPath, csv, StandardCharsets.UTF_8);
        return new CsvTransactionRepository(csvPath);
    }

    private static String csvWithRow(String row) {
        return HEADER + "\n" + row + "\n";
    }
}
