package com.oprica.tmsapi.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.oprica.tmsapi.model.TransactionStatus.PENDING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TransactionTest {

    @Test
    void constructor_whenAccountHolderNameIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Transaction(
                LocalDate.of(2026, 12, 7),
                "1234-5678-9101",
                "",
                new BigDecimal("1000.0"),
                PENDING
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountHolderName must not be blank");
    }

    @Test
    void constructor_whenAccountNumberIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Transaction(
                LocalDate.of(2026, 12, 7),
                "   ",
                "Test Holder",
                new BigDecimal("1000.0"),
                PENDING
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountNumber must not be blank");
    }
}
