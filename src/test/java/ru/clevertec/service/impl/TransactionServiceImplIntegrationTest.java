package ru.clevertec.service.impl;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;

import static org.assertj.core.api.Assertions.*;

public class TransactionServiceImplIntegrationTest {
    private final BaseIntegrationTest baseIntegrationTest = new BaseIntegrationTest();
    private static TransactionService service;

    @BeforeAll
    static void beforeAll() {
        BeanFactory factory = BeanFactory.INSTANCE;
        service = (TransactionService) factory.getBean("transactionService");
    }

    @BeforeEach
    void setUp() {
        baseIntegrationTest.runMigration();
    }

    @Test
    void checkTransferShouldThrowNotFoundExcAccountFromNull() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setToNumber("BLRB000000000000000000000001");
        data.setAmount(BigDecimal.ONE);
        data.setCurrency(Currency.BYN);
        Assertions.assertThrows(NotFoundException.class, () -> service.transfer(data));
    }

    @Test
    void checkTransferShouldThrowNotFoundExcAccountToNull() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("BLRB000000000000000000000001");
        data.setToNumber("1");
        data.setAmount(BigDecimal.ONE);
        data.setCurrency(Currency.BYN);
        Assertions.assertThrows(NotFoundException.class, () -> service.transfer(data));
    }

    @Test
    void checkTransferShouldThrowTransactionException() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("BLRB000000000000000000000001");
        data.setToNumber("BLRB000000000000000000000002");
        data.setAmount(BigDecimal.valueOf(99999999.99));
        data.setCurrency(Currency.GDP);
        Assertions.assertThrows(TransactionException.class, () -> service.transfer(data));
    }

    @Test
    void checkTransferShouldReturnEquals() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("BLRB000000000000000000000001");
        data.setToNumber("CLBP000000000000000000000009");
        data.setAmount(BigDecimal.TEN);
        data.setCurrency(Currency.GDP);
        ReceiptDto actual = service.transfer(data);
        BigDecimal amountInSenderCurrency = actual.getAmount();
        assertThat(amountInSenderCurrency.compareTo(data.getAmount())).isEqualTo(1);
    }

    @Test
    void checkTopUpShouldThrowNotFoundExc() {
        TransactionDto data = new TransactionDto();
        data.setToNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.topUp(data));
    }

    @Test
    void checkTopUpShouldReturnEquals() {
        TransactionDto data = new TransactionDto();
        data.setToNumber("BLRB000000000000000000000001");
        data.setCurrency(Currency.BYN);
        data.setAmount(BigDecimal.ONE);
        ReceiptDto actual = service.topUp(data);
        assertThat(actual.getTransactionType()).isEqualTo("top up");
    }

    @Test
    void checkWithdrawShouldSuccessAndAmountInAccountCurrencyMoreThanInTransactionAmountCurrency() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("BLRB000000000000000000000001");
        data.setCurrency(Currency.GDP);
        data.setAmount(BigDecimal.ONE);
        ReceiptDto dto = service.withdraw(data);
        BigDecimal amountInAccountCurrency = dto.getAmount();
        assertThat(amountInAccountCurrency.compareTo(data.getAmount())).isEqualTo(1);
    }

    @Test
    void checkWithdrawShouldThrowNotFoundExc() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setCurrency(Currency.GDP);
        data.setAmount(BigDecimal.ONE);
        Assertions.assertThrows(NotFoundException.class, () -> service.withdraw(data));
    }

    @Test
    void checkWithdrawShouldThrowTransactionExc() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("BLRB000000000000000000000001");
        data.setCurrency(Currency.GDP);
        data.setAmount(BigDecimal.valueOf(9999999.99));
        Assertions.assertThrows(TransactionException.class, () -> service.withdraw(data));
    }
}
