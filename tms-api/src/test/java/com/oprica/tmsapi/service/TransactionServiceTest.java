package com.oprica.tmsapi.service;

import com.oprica.tmsapi.dto.TransactionCreateRequest;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.model.TransactionStatus;
import com.oprica.tmsapi.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RandomTransactionStatusResolver statusResolver;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getAllTransactions_returnsRepositoryTransactions() {
        List<Transaction> transactions = List.of(
                new Transaction(
                        LocalDate.of(2025, 3, 1),
                        "7289-3445-1121",
                        "Maria Johnson",
                        new BigDecimal("150.00"),
                        TransactionStatus.SETTLED
                )
        );

        when(transactionRepository.findAll())
                .thenReturn(transactions);

        List<Transaction> result =
                transactionService.getAllTransactions();

        assertThat(result).isSameAs(transactions);
        verify(transactionRepository).findAll();
    }

    @Test
    void createTransaction_assignsResolvedStatusAndSavesTransaction() {
        TransactionCreateRequest request =
                new TransactionCreateRequest(
                        LocalDate.of(2026, 12, 7),
                        "1234-5678-9101",
                        "Test Holder",
                        new BigDecimal("1000.00")
                );

        Transaction expected = new Transaction(
                request.transactionDate(),
                request.accountNumber(),
                request.accountHolderName(),
                request.amount(),
                TransactionStatus.PENDING
        );

        when(statusResolver.resolve())
                .thenReturn(TransactionStatus.PENDING);
        when(transactionRepository.save(expected))
                .thenReturn(expected);

        Transaction result =
                transactionService.createTransaction(request);

        assertThat(result).isEqualTo(expected);

        verify(statusResolver).resolve();
        verify(transactionRepository).save(expected);
    }
}
