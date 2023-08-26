package ru.clevertec.service;

import java.util.List;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountStatementCreateDto;
import ru.clevertec.service.dto.AccountStatementDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.web.util.PagingUtil.Paging;

public interface AccountService {

    AccountDto create(AccountCreateDto dto);

    AccountDto getById(Long id);

    List<AccountDto> getAll(Paging paging);

    AccountDto update(AccountUpdateDto dto);

    void delete(Long id);

    AccountStatementDto getAccountStatement(AccountStatementCreateDto createDto);
}
