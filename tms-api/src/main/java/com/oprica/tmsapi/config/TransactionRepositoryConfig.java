package com.oprica.tmsapi.config;

import com.oprica.tmsapi.properties.TmsProperties;
import com.oprica.tmsapi.repository.CsvTransactionRepository;
import com.oprica.tmsapi.repository.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionRepositoryConfig {

    @Bean
    public TransactionRepository transactionRepository(TmsProperties properties) {
        CsvTransactionRepository repository = new CsvTransactionRepository(properties.storage().csvPath());
        repository.validate();
        return repository;
    }
}
