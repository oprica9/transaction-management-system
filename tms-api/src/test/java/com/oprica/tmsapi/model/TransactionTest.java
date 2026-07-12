package com.oprica.tmsapi.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static com.oprica.tmsapi.model.TransactionStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final LocalDate VALID_DATE =
            LocalDate.of(2026, 12, 7);

    private static final String VALID_ACCOUNT_NUMBER =
            "1234-5678-9101";

    private static final String VALID_ACCOUNT_HOLDER_NAME =
            "Test Holder";

    private static final BigDecimal VALID_AMOUNT =
            new BigDecimal("1000.00");

    @Test
    void constructor_whenValuesAreValid_createsTransaction() {
        Transaction transaction = new Transaction(
                VALID_DATE,
                VALID_ACCOUNT_NUMBER,
                VALID_ACCOUNT_HOLDER_NAME,
                VALID_AMOUNT,
                PENDING
        );

        assertThat(transaction.transactionDate())
                .isEqualTo(VALID_DATE);

        assertThat(transaction.accountNumber())
                .isEqualTo(VALID_ACCOUNT_NUMBER);

        assertThat(transaction.accountHolderName())
                .isEqualTo(VALID_ACCOUNT_HOLDER_NAME);

        assertThat(transaction.amount())
                .isEqualByComparingTo(VALID_AMOUNT);

        assertThat(transaction.status())
                .isEqualTo(PENDING);
    }

    @Test
    void constructor_whenTextValuesContainOuterWhitespace_trimsValues() {
        Transaction transaction = new Transaction(
                VALID_DATE,
                "  1234-5678-9101  ",
                "  Test Holder  ",
                VALID_AMOUNT,
                PENDING
        );

        assertThat(transaction.accountNumber())
                .isEqualTo("1234-5678-9101");

        assertThat(transaction.accountHolderName())
                .isEqualTo("Test Holder");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validAccountNumbers")
    void constructor_whenAccountNumberIsNonBlank_acceptsAnyFormat(
            String description,
            String accountNumber
    ) {
        Transaction transaction = new Transaction(
                VALID_DATE,
                accountNumber,
                VALID_ACCOUNT_HOLDER_NAME,
                VALID_AMOUNT,
                PENDING
        );

        assertThat(transaction.accountNumber())
                .isEqualTo(accountNumber.strip());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTransactions")
    void constructor_whenTransactionIsInvalid_throwsIllegalArgumentException(
            String description,
            LocalDate transactionDate,
            String accountNumber,
            String accountHolderName,
            BigDecimal amount,
            TransactionStatus status,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> new Transaction(
                transactionDate,
                accountNumber,
                accountHolderName,
                amount,
                status
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> validAccountNumbers() {
        return Stream.of(
                Arguments.of(
                        "sample formatted account number",
                        "1234-5678-9101"
                ),
                Arguments.of(
                        "short account identifier",
                        "1234567"
                ),
                Arguments.of(
                        "identifier containing letters",
                        "ACCOUNT-ABC-123"
                ),
                Arguments.of(
                        "IBAN-like identifier",
                        "RS35105008123123123173"
                ),
                Arguments.of(
                        "identifier containing spaces",
                        "1234 5678 9012"
                ),
                Arguments.of(
                        "long nonblank identifier",
                        "A".repeat(100)
                ),
                Arguments.of(
                        "identifier with outer whitespace",
                        "  ABC-123  "
                )
        );
    }

    private static Stream<Arguments> invalidTransactions() {
        return Stream.of(
                Arguments.of(
                        "transaction date is null",
                        null,
                        VALID_ACCOUNT_NUMBER,
                        VALID_ACCOUNT_HOLDER_NAME,
                        VALID_AMOUNT,
                        PENDING,
                        "transactionDate must not be null"
                ),
                Arguments.of(
                        "account number is null",
                        VALID_DATE,
                        null,
                        VALID_ACCOUNT_HOLDER_NAME,
                        VALID_AMOUNT,
                        PENDING,
                        "accountNumber must not be blank"
                ),
                Arguments.of(
                        "account number is empty",
                        VALID_DATE,
                        "",
                        VALID_ACCOUNT_HOLDER_NAME,
                        VALID_AMOUNT,
                        PENDING,
                        "accountNumber must not be blank"
                ),
                Arguments.of(
                        "account number contains only whitespace",
                        VALID_DATE,
                        "   ",
                        VALID_ACCOUNT_HOLDER_NAME,
                        VALID_AMOUNT,
                        PENDING,
                        "accountNumber must not be blank"
                ),
                Arguments.of(
                        "account holder name is null",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        null,
                        VALID_AMOUNT,
                        PENDING,
                        "accountHolderName must not be blank"
                ),
                Arguments.of(
                        "account holder name is empty",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        "",
                        VALID_AMOUNT,
                        PENDING,
                        "accountHolderName must not be blank"
                ),
                Arguments.of(
                        "account holder name contains only whitespace",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        "   ",
                        VALID_AMOUNT,
                        PENDING,
                        "accountHolderName must not be blank"
                ),
                Arguments.of(
                        "amount is null",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        VALID_ACCOUNT_HOLDER_NAME,
                        null,
                        PENDING,
                        "amount must not be null"
                ),
                Arguments.of(
                        "amount is zero",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        VALID_ACCOUNT_HOLDER_NAME,
                        BigDecimal.ZERO,
                        PENDING,
                        "amount must be greater than zero"
                ),
                Arguments.of(
                        "amount is negative",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        VALID_ACCOUNT_HOLDER_NAME,
                        new BigDecimal("-0.01"),
                        PENDING,
                        "amount must be greater than zero"
                ),
                Arguments.of(
                        "status is null",
                        VALID_DATE,
                        VALID_ACCOUNT_NUMBER,
                        VALID_ACCOUNT_HOLDER_NAME,
                        VALID_AMOUNT,
                        null,
                        "status must not be null"
                )
        );
    }
}
