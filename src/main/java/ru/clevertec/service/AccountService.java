package ru.clevertec.service;

import java.util.List;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.web.util.PagingUtil.Paging;

public interface AccountService {

    AccountDto create(AccountCreateDto dto);

    AccountDto findById(Long id);

    List<AccountDto> findAll(Paging paging);

    AccountDto update(AccountUpdateDto dto);

    void deleteById(Long id);

    ExtractDto getExtract(ExtractStatementCreateDto createDto);

    StatementDto getMoneyStatement(ExtractStatementCreateDto createDto);

    void accrueInterest();

}
