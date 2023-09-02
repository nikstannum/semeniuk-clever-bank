package ru.clevertec.service.impl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.repository.impl.BankRepositoryImpl;
import ru.clevertec.service.dto.BankCreateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.BankUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {
    @Mock
    private BankRepositoryImpl repository;
    @InjectMocks
    private BankServiceImpl service;

    @Test
    void checkFindByIdShouldReturnEquals() {
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("Belarusbank");
        bank.setBankIdentifier("BLRB");
        bank.setDeleted(false);
        doReturn(Optional.of(bank)).when(repository).findById(1L);
        BankDto expected = new BankDto();
        expected.setId(1L);
        expected.setBankIdentifier("BLRB");
        expected.setName("Belarusbank");

        BankDto actual = service.findById(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkFindByIdShouldThrowNotFoundExc() {
        doReturn(Optional.empty()).when(repository).findById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void checkFindAllShouldHasSize2() {
        doReturn(List.of(new Bank(), new Bank())).when(repository).findAll(2, 0);
        Paging paging = new Paging(2, 0);
        List<BankDto> actualList = service.findAll(paging);
        assertThat(actualList).hasSize(2);
    }

    @Test
    void checkCreateShouldThrowEntityExistsExc() {
        doReturn(Optional.of(new Bank())).when(repository).findByIdentifier("BLRB");
        BankCreateDto dto = new BankCreateDto();
        dto.setBankIdentifier("BLRB");
        Assertions.assertThrows(EntityExistsException.class, () -> service.create(dto));
    }

    @Test
    void checkCreateShouldReturnEquals() {
        doReturn(Optional.empty()).when(repository).findByIdentifier("CLBN");
        Bank bank = new Bank();
        bank.setName("Clever-bank");
        bank.setBankIdentifier("CLBN");
        doReturn(bank).when(repository).create(bank);
        BankDto expected = new BankDto();
        expected.setBankIdentifier("CLBN");
        expected.setName("Clever-bank");
        BankCreateDto createDto = new BankCreateDto();
        createDto.setName("Clever-bank");
        createDto.setBankIdentifier("CLBN");

        BankDto actual = service.create(createDto);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkUpdateShouldThrowNotFoundExc() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(1L);
        doReturn(Optional.empty()).when(repository).findById(1L);
        Assertions.assertThrows(NotFoundException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldThrowEntityExistsExc() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(1L);
        dto.setBankIdentifier("BLRB");
        Bank updatable = new Bank();
        updatable.setId(1L);
        doReturn(Optional.of(updatable)).when(repository).findById(1L);
        Bank existing = new Bank();
        existing.setBankIdentifier("BLRB");
        existing.setId(2L);
        doReturn(Optional.of(existing)).when(repository).findByIdentifier("BLRB");
        Assertions.assertThrows(EntityExistsException.class, () -> service.update(dto));
    }

    @Test
    void checkUpdateShouldReturnEquals() {
        BankUpdateDto dto = new BankUpdateDto();
        dto.setId(1L);
        dto.setBankIdentifier("BLRB");
        dto.setName("New-bank-name");
        Bank updatable = new Bank();
        updatable.setId(1L);
        updatable.setBankIdentifier("AKBB");
        updatable.setName("Old-bank-name");
        doReturn(Optional.of(updatable)).when(repository).findById(1L);
        doReturn(Optional.empty()).when(repository).findByIdentifier("BLRB");
        Bank bank = new Bank();
        bank.setId(1L);
        bank.setName("New-bank-name");
        bank.setBankIdentifier("BLRB");
        doReturn(bank).when(repository).update(bank);
        BankDto actual = service.update(dto);
        assertThat(actual.getBankIdentifier()).isEqualTo("BLRB");
    }

    @Captor
    ArgumentCaptor<Long> captor;

    @Test
    void checkDeleteByIdShouldCaptured() {
        service.deleteById(1L);
        verify(repository).deleteById(captor.capture());
        Long captured = captor.getValue();
        assertThat(captured).isEqualTo(1L);

    }
}