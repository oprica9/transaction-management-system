package com.oprica.tmsapi.service;

import com.oprica.tmsapi.model.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Random;

@Component
public class RandomTransactionStatusResolver {

    private static final TransactionStatus[] STATUSES = TransactionStatus.values();

    private final Random random;

    public RandomTransactionStatusResolver() {
        this(new Random());
    }

    RandomTransactionStatusResolver(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public TransactionStatus resolve() {
        return STATUSES[random.nextInt(STATUSES.length)];
    }
}
