package com.oprica.tmsapi.service;

import com.oprica.tmsapi.model.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RandomTransactionStatusResolverTest {

    @Test
    void resolve_returnsStatusAtRandomlySelectedIndex() {
        Random random = mock(Random.class);

        when(random.nextInt(TransactionStatus.values().length))
                .thenReturn(1);

        RandomTransactionStatusResolver resolver = new RandomTransactionStatusResolver(random);

        TransactionStatus result = resolver.resolve();

        assertThat(result)
                .isEqualTo(TransactionStatus.SETTLED);

        verify(random)
                .nextInt(TransactionStatus.values().length);
    }
}
