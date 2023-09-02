package ru.clevertec.service.impl;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.BankService;
import ru.clevertec.service.dto.BankCreateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.BankUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;

public class BankServiceImplIntegrationTest {
    private final BaseIntegrationTest baseIntegrationTest = new BaseIntegrationTest();
    private static BankService service;

    @BeforeAll
    static void beforeAll() {
        BeanFactory factory = BeanFactory.INSTANCE;
        service = (BankService) factory.getBean("bankService");
    }

    @BeforeEach
    void setUp() {
        baseIntegrationTest.runMigration();
    }

    @Test
    void findByIdShouldThrowNotFoundExc() {
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(0L));
    }

    @Test
    void findByIdShouldReturnNotNull() {
        BankDto bankDto = service.findById(1L);
        assertThat(bankDto).isNotNull();
    }

    @Test
    void checkFindAllShouldHasSize2() {
        Paging paging = new Paging(2, 0);
        List<BankDto> list = service.findAll(paging);
        assertThat(list).hasSize(2);
    }

    @Test
    void checkCreateShouldThrowEntityExistsException() {
        BankCreateDto dto = new BankCreateDto();
        dto.setBankIdentifier("BLRB");
        dto.setName("new bank");
        Assertions.assertThrows(EntityExistsException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldReturnNotNull() {
        BankCreateDto dto = new BankCreateDto();
        dto.setBankIdentifier("IIII");
        dto.setName("new bank");
        BankDto actual = service.create(dto);
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void checkUpdateShouldThrowNotFoundExc() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(0L);
        dto.setBankIdentifier("IIII");
        dto.setName("new bank");
        Assertions.assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldThrowEntityExistsExc() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(2L);
        dto.setBankIdentifier("BLRB");
        dto.setName("new bank");
        Assertions.assertThrows(EntityExistsException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(1L);
        dto.setBankIdentifier("IIII");
        dto.setName("new bank");
        BankDto actual = service.update(dto);
        assertThat(actual.getBankIdentifier()).isEqualTo("IIII");
    }

    @Test
    void checkDeleteByIdShouldSuccess() {
        service.deleteById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

}
