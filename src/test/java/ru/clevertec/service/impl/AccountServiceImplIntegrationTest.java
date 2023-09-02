package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceImplIntegrationTest {
    private static AccountService service;
    private final BaseIntegrationTest baseIntegrationTest = new BaseIntegrationTest();

    @BeforeAll
    static void beforeAll() {
        BeanFactory factory = BeanFactory.INSTANCE;
        service = (AccountService) factory.getBean("accountService");
    }

    @BeforeEach
    void setUp() {
        baseIntegrationTest.runMigration();
    }

    @Test
    void checkAccrueInterest() {
        AccountDto beforeAccrue = service.findById(1L);
        service.accrueInterest();
        AccountDto afterAccrue = service.findById(1L);
        BigDecimal before = beforeAccrue.getAmount();
        BigDecimal after = afterAccrue.getAmount();
        assertThat(after.compareTo(before)).isEqualTo(1);
    }

    @Test
    void checkFindByIdShouldReturnNotNull() {
        AccountDto actual = service.findById(1L);
        assertThat(actual).isNotNull();
    }

    @Test
    void checkFindByIdShouldThrowNotFoundExc() {
        assertThrows(NotFoundException.class, () -> service.findById(0L));
    }

    @Test
    void checkFindAllShouldHasSize2() {
        Paging paging = new Paging(2, 0);
        List<AccountDto> list = service.findAll(paging);
        assertThat(list).hasSize(2);
    }

    @Test
    void checkDeleteByIdShouldSuccess() {
        service.deleteById(1L);
        assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void checkUpdateShouldThrowNotFoundExcBank() {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setId(1L);
        dto.setBankIdentifier("IIII");
        dto.setAmount(BigDecimal.ONE);
        assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldThrowNotFoundExcUser() {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setId(0L);
        dto.setBankIdentifier("CLBN");
        dto.setAmount(BigDecimal.ONE);
        assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setId(1L);
        dto.setBankIdentifier("CLBP");
        dto.setAmount(BigDecimal.ONE);
        AccountDto actual = service.update(dto);
        assertThat(actual.getAmount()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void checkCreateShouldThrowNotFoundExcBank() {
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("ivanov@mail.com");
        dto.setBankIdentifier("IIII");
        assertThrows(NotFoundException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldThrowNotFoundExcUser() {
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("invalid@mail.com");
        dto.setBankIdentifier("CLBN");
        assertThrows(NotFoundException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldReturnNotNull() {
        AccountCreateDto dto = new AccountCreateDto();
        dto.setEmail("ivanov@mail.com");
        dto.setBankIdentifier("CLBP");
        dto.setCurrency(Currency.BYN);
        AccountDto actual = service.create(dto);
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void checkGetExtractShouldThrowNotFoundExc() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        assertThrows(NotFoundException.class, () -> service.getExtract(dto));
    }

    @Test
    void checkGetExtractShouldReturnNotNull() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("BLRB000000000000000000000001");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        ExtractDto actual = service.getExtract(dto);
        assertThat(actual.getMoneyMovement()).isNotNull();
    }

    @Test
    void checkGetStatementShouldThrowNotFoundExc() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("1");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        assertThrows(NotFoundException.class, () -> service.getMoneyStatement(dto));
    }

    @Test
    void checkGetStatementShouldReturnNotNull() {
        ExtractStatementCreateDto dto = new ExtractStatementCreateDto();
        dto.setAccountNumber("BLRB000000000000000000000001");
        dto.setPeriodFrom(LocalDate.now().minusDays(1));
        dto.setPeriodTo(LocalDate.now());
        StatementDto actual = service.getMoneyStatement(dto);
        assertThat(actual.getIncomeExpense().get("expense")).isZero();
    }
}
