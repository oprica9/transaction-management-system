package com.oprica.tmsapi.service;

import com.oprica.tmsapi.dto.TransactionCreateRequest;
import com.oprica.tmsapi.model.Transaction;
import com.oprica.tmsapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RandomTransactionStatusResolver statusResolver;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction createTransaction(TransactionCreateRequest request) {
        Transaction transaction = new Transaction(
                request.transactionDate(),
                request.accountNumber(),
                request.accountHolderName(),
                request.amount(),
                statusResolver.resolve()
        );

        return transactionRepository.save(transaction);
    }
}
