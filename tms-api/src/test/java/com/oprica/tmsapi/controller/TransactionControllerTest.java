package com.oprica.tmsapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oprica.tmsapi.dto.TransactionCreateRequest;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.model.TransactionStatus;
import com.oprica.tmsapi.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        "1234-5678-9101",
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
                        .value("1234-5678-9101"))
                .andExpect(jsonPath("$.accountHolderName")
                        .value("Test Holder"))
                .andExpect(jsonPath("$.amount")
                        .value(1000.00))
                .andExpect(jsonPath("$.status")
                        .value("Pending"));

        verify(transactionService).createTransaction(request);
    }

    @Test
    void createTransaction_whenAccountNumberHasInvalidFormat_returnsBadRequest() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "123456789012",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenAccountNumberIsBlank_returnsBadRequest() throws Exception {
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
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenAccountHolderNameIsBlank_returnsBadRequest() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "1234-5678-9101",
                  "accountHolderName": " ",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenAmountIsZero_returnsBadRequest() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "1234-5678-9101",
                  "accountHolderName": "Test Holder",
                  "amount": 0
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenAmountIsNegative_returnsBadRequest() throws Exception {
        String request = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "1234-5678-9101",
                  "accountHolderName": "Test Holder",
                  "amount": -10.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenRequiredFieldIsMissing_returnsBadRequest() throws Exception {
        String request = """
                {
                  "accountNumber": "1234-5678-9101",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenDateHasInvalidFormat_returnsBadRequest() throws Exception {
        String request = """
                {
                  "transactionDate": "07-12-2026",
                  "accountNumber": "1234-5678-9101",
                  "accountHolderName": "Test Holder",
                  "amount": 1000.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void createTransaction_whenBodyIsMalformedJson_returnsBadRequest() throws Exception {
        String malformedRequest = """
                {
                  "transactionDate": "2026-12-07",
                  "accountNumber": "1234-5678-9101"
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }
}
