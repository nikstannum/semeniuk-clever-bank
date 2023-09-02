package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.impl.AccountRepositoryImpl;
import ru.clevertec.data.repository.impl.BankRepositoryImpl;
import ru.clevertec.data.repository.impl.TransactionRepositoryImpl;
import ru.clevertec.data.repository.impl.UserRepositoryImpl;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Captor
    ArgumentCaptor<Long> captor;
    @Mock
    private AccountRepositoryImpl accountRepository;
    @Mock
    private BankRepositoryImpl bankRepository;
    @Mock
    private UserRepositoryImpl userRepository;
    @Mock
    private TransactionRepositoryImpl transactionRepository;
    @InjectMocks
    private AccountServiceImpl service;

    @Test
    void accrueInterest() {

    }

    @Test
    void checkFindByIdShouldReturnEquals() {
        Account account = getAccount();
        doReturn(Optional.of(account)).when(accountRepository).findById(1L);
        AccountDto actual = service.findById(1L);
        assertThat(actual.getUser().getEmail()).isEqualTo("email@email.com");
    }

    private Account getAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAmount(BigDecimal.ONE);
        account.setCurrency(Currency.BYN);
        account.setNumber("1");
        account.setDeleted(false);
        account.setOpenTime(LocalDate.now());
        account.setUser(getUser());
        account.setBank(getBank());
        return account;
    }

    @Test
    void checkFindByIdShouldThrowNotFoundExc() {
        doReturn(Optional.empty()).when(accountRepository).findById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email@email.com");
        user.setDeleted(false);
        return user;
    }

    private Bank getBank() {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("Clever-bank");
        bank.setBankIdentifier("CLBN");
        return bank;
    }

    @Test
    void checkFindAllShouldHasSize2() {
        doReturn(List.of(getAccount(), getAccount())).when(accountRepository).findAll(2, 0);
        Paging paging = new Paging(2, 0);
        List<AccountDto> actualList = service.findAll(paging);
        assertThat(actualList).hasSize(2);
    }

    @Test
    void deleteById() {
        service.deleteById(1L);
        verify(accountRepository).deleteById(captor.capture());
        Long captured = captor.getValue();
        assertThat(captured).isEqualTo(1L);
    }

    private AccountUpdateDto getAccountUpdateDto() {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setId(1L);
        dto.setAmount(BigDecimal.ONE);
        dto.setBankIdentifier("CLBN");
        return dto;
    }

    @Test
    void checkUpdateShouldThrowNotFoundExcBank() {
        AccountUpdateDto dto = getAccountUpdateDto();
        doReturn(Optional.empty()).when(bankRepository).findByIdentifier("CLBN");
        Assertions.assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldThrowNotFoundExcAccount() {
        AccountUpdateDto dto = getAccountUpdateDto();
        Bank bank = getBank();
        doReturn(Optional.of(bank)).when(bankRepository).findByIdentifier("CLBN");
        doReturn(Optional.empty()).when(accountRepository).findById(dto.getId());
        Assertions.assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        AccountUpdateDto dto = getAccountUpdateDto();
        Bank bank = getBank();
        doReturn(Optional.of(bank)).when(bankRepository).findByIdentifier("CLBN");
        Account account = getAccount();
        doReturn(Optional.of(account)).when(accountRepository).findById(dto.getId());
        account.setBank(bank);
        account.setAmount(BigDecimal.ONE);
        doReturn(account).when(accountRepository).update(account);

        AccountDto actual = service.update(dto);

        assertThat(actual.getBank().getBankIdentifier()).isEqualTo("CLBN");
    }

    @Test
    void checkCreateShouldReturnEquals() {
        User user = getUser();
        doReturn(Optional.of(user)).when(userRepository).findUserByEmail("email@email.com");
        Bank bank = getBank();
        doReturn(Optional.of(bank)).when(bankRepository).findByIdentifier("CLBN");
        Account account = new Account();
        account.setUser(user);
        account.setBank(bank);
        account.setAmount(BigDecimal.ZERO);
        account.setCurrency(Currency.BYN);
        doReturn(account).when(accountRepository).create(account);
        AccountCreateDto createDto = new AccountCreateDto();
        createDto.setEmail("email@email.com");
        createDto.setBankIdentifier("CLBN");
        createDto.setCurrency(Currency.BYN);

        AccountDto actual = service.create(createDto);

        assertThat(actual.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void checkCreateShouldThrowNotFoundExcUser() {
        doReturn(Optional.empty()).when(userRepository).findUserByEmail("invalid@email.com");
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("invalid@email.com");
        Assertions.assertThrows(NotFoundException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldThrowNotFoundExcBank() {
        doReturn(Optional.of(getUser())).when(userRepository).findUserByEmail("email@email.com");
        doReturn(Optional.empty()).when(bankRepository).findByIdentifier("CLBN");
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("email@email.com");
        dto.setBankIdentifier("CLBN");
        Assertions.assertThrows(NotFoundException.class, () -> service.create(dto));
    }

    private Transaction getTransaction(Long id) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAccountFrom(getAccount());
        transaction.setAccountTo(getAccount());
        transaction.setAccountAmountFrom(BigDecimal.ONE);
        transaction.setAccountAmountTo(BigDecimal.ONE);
        transaction.setTransactionTime(Instant.now());
        return transaction;
    }

    @Test
    void checkGetExtractShouldHasSize4() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        doReturn(Optional.of(getAccount())).when(accountRepository).findByNumber("1");
        Transaction tr1 = getTransaction(1L);

        Transaction tr2 = getTransaction(2L);
        Account accountFromTr2 = getAccount();
        accountFromTr2.setId(2L);
        tr2.setAccountFrom(accountFromTr2);

        Transaction tr3 = getTransaction(3L);
        tr3.setAccountAmountFrom(null);

        Transaction tr4 = getTransaction(4L);
        tr4.setAccountAmountTo(null);

        List<Transaction> transactions = Arrays.asList(tr1, tr2, tr3, tr4);
        doReturn(transactions).when(transactionRepository).findAllTransactionsForUser(any(), any(), eq(1L));

        ExtractDto actual = service.getExtract(dto);
        assertThat(actual.getMoneyMovement()).hasSize(4);
    }

    @Test
    void checkGetExtractShouldThrowRuntimeExc() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        doReturn(Optional.of(getAccount())).when(accountRepository).findByNumber("1");
        Transaction transaction = getTransaction(1L);
        transaction.setAccountAmountFrom(null);
        transaction.setAccountAmountTo(null);
        List<Transaction> transactions = List.of(transaction);
        doReturn(transactions).when(transactionRepository).findAllTransactionsForUser(any(), any(), eq(1L));
        Assertions.assertThrows(RuntimeException.class, () -> service.getExtract(dto));
    }

    @Test
    void checkGetExtractShouldThrowNotFoundExc() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        doReturn(Optional.empty()).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.getExtract(dto));
    }

    @Test
    void checkGetMoneyStatementShouldReturnEquals() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        doReturn(Optional.of(getAccount())).when(accountRepository).findByNumber("1");
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("income", BigDecimal.TEN);
        map.put("expense", BigDecimal.TEN);
        doReturn(map).when(transactionRepository).findIncomeAndExpenseForUser(any(), any(), eq(1L));
        StatementDto actual = service.getMoneyStatement(dto);
        assertThat(actual.getIncomeExpense().get("expense")).isEqualTo(BigDecimal.TEN.negate());
    }

    @Test
    void checkGetMoneyStatementShouldThrowNotFoundExc() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        doReturn(Optional.empty()).when(accountRepository).findByNumber("1");
        Assertions.assertThrows(NotFoundException.class, () -> service.getExtract(dto));
    }
}