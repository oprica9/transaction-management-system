package com.oprica.tmsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oprica.tmsapi.dto.TransactionCreateRequest;
import com.oprica.tmsapi.exception.InvalidTransactionCsvException;
import com.oprica.tmsapi.exception.TransactionRepositoryException;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.model.TransactionStatus;
import com.oprica.tmsapi.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final Transaction TRANSACTION =
            new Transaction(
                    LocalDate.of(2025, 3, 1),
                    "7289-3445-1121",
                    "Maria Johnson",
                    new BigDecimal("150.00"),
                    TransactionStatus.SETTLED
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void getAllTransactions_returnsTransactions() throws Exception {
        when(transactionService.getAllTransactions())
                .thenReturn(List.of(TRANSACTION));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transactionDate")
                        .value("2025-03-01"))
                .andExpect(jsonPath("$[0].accountNumber")
                        .value("7289-3445-1121"))
                .andExpect(jsonPath("$[0].accountHolderName")
                        .value("Maria Johnson"))
                .andExpect(jsonPath("$[0].amount")
                        .value(150.00))
                .andExpect(jsonPath("$[0].status")
                        .value("Settled"));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void getAllTransactions_whenNoTransactionsExist_returnsEmptyArray() throws Exception {
        when(transactionService.getAllTransactions())
                .thenReturn(List.of());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(transactionService).getAllTransactions();
    }

    @Test
    void createTransaction_whenRequestIsValid_returnsCreatedTransaction() throws Exception {
        TransactionCreateRequest request =
                new TransactionCreateRequest(
                        LocalDate.of(2026, 12, 7),
                        "ACCOUNT-123",
                        "Test Holder",
                        new BigDecimal("1000.00")
                );

        Transaction createdTransaction =
                new Transaction(
                        request.transactionDate(),
                        request.accountNumber(),
                        request.accountHolderName(),
                        request.amount(),
                        TransactionStatus.PENDING
                );

        when(transactionService.createTransaction(request))
                .thenReturn(createdTransaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionDate")
                        .value("2026-12-07"))
                .andExpect(jsonPath("$.accountNumber")
                        .value("ACCOUNT-123"))
                .andExpect(jsonPath("$.accountHolderName")
                        .value("Test Holder"))
                .andExpect(jsonPath("$.amount")
                        .value(1000.00))
                .andExpect(jsonPath("$.status")
                        .value("Pending"));

        verify(transactionService).createTransaction(request);
    }

    @Test
    void createTransaction_whenAccountNumberIsBlank_returnsValidationProblem() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "   ",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.accountNumber")
                        .isArray())
                .andExpect(jsonPath("$.errors.accountNumber[0]")
                        .value("must not be blank"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenAccountHolderNameIsBlank_returnsValidationProblem() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "ACCOUNT-123",
                  "accountHolderName": "   ",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.accountHolderName")
                        .isArray())
                .andExpect(jsonPath("$.errors.accountHolderName[0]")
                        .value("must not be blank"));

        verifyNoInteractions(transactionService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10.00"})
    void createTransaction_whenAmountIsNotPositive_returnsValidationProblem(String amount
    ) throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "ACCOUNT-123",
                  "accountHolderName": "Test Holder",
                  "amount": %s
                }
                """.formatted(amount);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.amount")
                        .isArray())
                .andExpect(jsonPath("$.errors.amount[0]")
                        .value("Amount must be greater than zero"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenTransactionDateIsMissing_returnsValidationProblem() throws Exception {
        String request = """
                {
                  "accountNumber": "ACCOUNT-123",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.transactionDate")
                        .isArray())
                .andExpect(jsonPath("$.errors.transactionDate[0]")
                        .value("must not be null"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenDateHasInvalidFormat_returnsMalformedRequestProblem() throws Exception {
        String request = """
                {
                  "transactionDate": "07-12-2026",
                  "accountNumber": "ACCOUNT-123",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"))
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenBodyIsMalformedJson_returnsMalformedRequestProblem() throws Exception {
        String malformedRequest = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "ACCOUNT-123"
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Invalid request body"))
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void getAllTransactions_whenCsvIsInvalid_returnsInvalidDataProblem() throws Exception {
        when(transactionService.getAllTransactions())
                .thenThrow(new InvalidTransactionCsvException("Sensitive CSV details"));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Invalid transaction data"))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_TRANSACTION_DATA"))
                .andExpect(jsonPath("$.detail")
                        .value("The stored transaction data is invalid."))
                .andExpect(content().string(not(containsString("Sensitive CSV details"))));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void getAllTransactions_whenRepositoryFails_returnsStorageFailureProblem() throws Exception {
        when(transactionService.getAllTransactions())
                .thenThrow(new TransactionRepositoryException("Failed to read /private/path.csv", new IOException("Permission denied")));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("Transaction storage failure"))
                .andExpect(jsonPath("$.code")
                        .value("TRANSACTION_STORAGE_FAILURE"))
                .andExpect(jsonPath("$.detail")
                        .value("The transaction storage operation could not be completed."))
                .andExpect(content().string(not(containsString("/private/path.csv"))));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void getAllTransactions_whenUnexpectedFailureOccurs_returnsGenericProblem() throws Exception {
        when(transactionService.getAllTransactions())
                .thenThrow(new IllegalStateException("Sensitive implementation detail"));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title")
                        .value("Internal server error"))
                .andExpect(jsonPath("$.code")
                        .value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail")
                        .value("An unexpected error occurred."))
                .andExpect(content().string(not(containsString("Sensitive implementation detail"))));

        verify(transactionService).getAllTransactions();
    }
}
