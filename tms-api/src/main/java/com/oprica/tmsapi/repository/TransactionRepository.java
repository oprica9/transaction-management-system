package com.oprica.tmsapi.repository;

import com.oprica.tmsapi.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    /**
     * Loads all transactions into memory.
     *
     * @return all stored transactions
     */
    List<Transaction> findAll();

    /**
     * Persists a new transaction.
     *
     * @param transaction transaction to persist
     */
    void save(Transaction transaction);
}
