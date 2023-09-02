package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.data.DbTransactionManagerImpl;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.repository.impl.AccountRepositoryImpl;
import ru.clevertec.data.repository.impl.TransactionRepositoryImpl;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;
import ru.clevertec.service.util.MoneyUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private TransactionRepositoryImpl transactionRepository;
    @Mock
    private AccountRepositoryImpl accountRepository;
    private TransactionServiceImpl service;


    @BeforeEach
    void setUp() {
        BeanFactory factory = BeanFactory.INSTANCE;
        DataSource dataSource = (DataSource) factory.getBean("dataSource");
        Map<String, BigDecimal> exchangeRates = new HashMap<>();
        exchangeRates.put("USD", BigDecimal.ONE);
        exchangeRates.put("BYN", BigDecimal.valueOf(0.5));
        exchangeRates.put("EUR", BigDecimal.valueOf(2));
        exchangeRates.put("GDP", BigDecimal.valueOf(4));
        service = new TransactionServiceImpl(transactionRepository, accountRepository, new DbTransactionManagerImpl(dataSource),
                new MoneyUtil(exchangeRates));
    }

    @Test
    void checkTransferShouldReturnEquals() {
        TransactionDto data = getStandardTransaction();
        Account accountFrom = getAccount(1L, "1");
        doReturn(Optional.of(accountFrom)).when(accountRepository).findByNumber("1");
        Account accountTo = getAccount(2L, "2");
        doReturn(Optional.of(accountTo)).when(accountRepository).findByNumber("2");
        doNothing().when(accountRepository).updateAmountByNumber(any(), any());
        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0, Transaction.class);
            transaction.setId(1L);
            transaction.setTransactionTime(Instant.now());
            return null;
        }).when(transactionRepository).createTransaction(any(), any());
        ReceiptDto actual = service.transfer(data);
        assertThat(actual.getTransactionType()).isEqualTo("transfer");
    }

    @Test
    void checkTransferShouldThrowNotFoundExcWhenAccountFromNotExists() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        doReturn(Optional.empty()).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.transfer(data));
    }

    @Test
    void checkTransferShouldThrowNotFoundExcWhenAccountToNotExists() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setToNumber("2");
        Account accountFrom = new Account();
        accountFrom.setNumber("1");
        doReturn(Optional.of(accountFrom)).when(accountRepository).findByNumber("1");
        doReturn(Optional.empty()).when(accountRepository).findByNumber("2");
        Assertions.assertThrows(NotFoundException.class, () -> service.transfer(data));
    }

    @Test
    void checkTransferShouldThrowTransactionExc() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setToNumber("2");
        data.setAmount(BigDecimal.ONE);
        data.setCurrency(Currency.USD);
        Account accountFrom = new Account();
        accountFrom.setNumber("1");
        accountFrom.setAmount(BigDecimal.valueOf(2));
        accountFrom.setCurrency(Currency.BYN);
        doReturn(Optional.of(accountFrom)).when(accountRepository).findByNumber("1");
        Account accountTo = new Account();
        accountTo.setNumber("2");
        accountTo.setCurrency(Currency.GDP);
        accountTo.setAmount(BigDecimal.ZERO);
        doReturn(Optional.of(accountTo)).when(accountRepository).findByNumber("2");
        Assertions.assertThrows(TransactionException.class, () -> service.transfer(data));
    }

    private Account getAccount(Long id, String number) {
        Account account = new Account();
        account.setId(id);
        account.setNumber(number);
        account.setAmount(BigDecimal.TEN);
        account.setCurrency(Currency.BYN);
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("Belarusbank");
        bank.setBankIdentifier("BLRB");
        account.setBank(bank);
        return account;
    }

    private TransactionDto getStandardTransaction() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setToNumber("2");
        data.setCurrency(Currency.BYN);
        data.setAmount(BigDecimal.ONE);
        return data;
    }

    @Test
    void checkTopUpShouldThrowNotFoundExc() {
        TransactionDto data = new TransactionDto();
        data.setToNumber("1");
        doReturn(Optional.empty()).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.topUp(data));
    }

    @Test
    void checkTopUpShouldReturnEquals() {
        TransactionDto data = new TransactionDto();
        data.setToNumber("1");
        data.setAmount(BigDecimal.ONE);
        data.setCurrency(Currency.USD);
        Account account = getAccount(1L, "1");
        doReturn(Optional.of(account)).when(accountRepository).findByNumber("1");
        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0, Transaction.class);
            transaction.setId(1L);
            transaction.setTransactionTime(Instant.now());
            return null;
        }).when(transactionRepository).createTransaction(any(), any());
        ReceiptDto actual = service.topUp(data);
        assertThat(actual.getTransactionType()).isEqualTo("top up");
    }

    @Test
    void checkWithdrawShouldThrowNotFoundExc() {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setFromNumber("1");
        doReturn(Optional.empty()).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.withdraw(transactionDto));
    }

    @Test
    void checkWithdrawShouldThrowTransactionExc() {
        TransactionDto data = new TransactionDto();
        data.setFromNumber("1");
        data.setAmount(BigDecimal.ONE);
        data.setCurrency(Currency.USD);
        Account accountFrom = new Account();
        accountFrom.setNumber("1");
        accountFrom.setAmount(BigDecimal.valueOf(2));
        accountFrom.setCurrency(Currency.BYN);
        doReturn(Optional.of(accountFrom)).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(TransactionException.class, () -> service.withdraw(data));
    }

    @Test
    void checkWithdrawShouldReturnEquals() {
        TransactionDto data = getStandardTransaction();
        Account accountFrom = getAccount(1L, "1");
        doReturn(Optional.of(accountFrom)).when(accountRepository).findByNumber("1");
        doNothing().when(accountRepository).updateAmountByNumber(any(), any());
        doAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0, Transaction.class);
            transaction.setId(1L);
            transaction.setTransactionTime(Instant.now());
            return null;
        }).when(transactionRepository).createTransaction(any(), any());
        ReceiptDto actual = service.withdraw(data);
        assertThat(actual.getTransactionType()).isEqualTo("withdrawal");
    }
}