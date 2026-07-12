package com.oprica.tmsapi.repository;

import com.oprica.tmsapi.exception.TransactionRepositoryException;
import com.oprica.tmsapi.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    /**
     * Loads all stored transactions in persistence order.
     *
     * @return a snapshot of all stored transactions
     * @throws TransactionRepositoryException if the transactions cannot be loaded
     */
    List<Transaction> findAll();

    /**
     * Persists a transaction.
     *
     * @param transaction transaction to persist
     * @return the persisted transaction
     * @throws NullPointerException           if {@code transaction} is {@code null}
     * @throws TransactionRepositoryException if the transaction cannot be persisted
     */
    Transaction save(Transaction transaction);
}
