package ru.clevertec.service;

import java.util.List;
import ru.clevertec.service.dto.BankCreateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.BankUpdateDto;
import ru.clevertec.web.util.PagingUtil.Paging;

public interface BankService {
    BankDto findById(Long id);

    List<BankDto> findAll(Paging paging);

    BankDto create(BankCreateDto dto);

    BankDto update(BankUpdateDto dto);

    void deleteById(Long id);
}
