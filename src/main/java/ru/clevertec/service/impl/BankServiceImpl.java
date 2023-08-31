package ru.clevertec.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.service.BankService;
import ru.clevertec.service.dto.BankCreateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.BankUpdateDto;
import ru.clevertec.service.exception.EntityExistsException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;

    @Override
    public BankDto findById(Long id) {
        return toDto(bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("wasn't found bank with id = " + id)));
    }

    private BankDto toDto(Bank entity) {
        BankDto dto = new BankDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBankIdentifier(entity.getBankIdentifier());
        return dto;
    }

    @Override
    public List<BankDto> findAll(Paging paging) {
        return bankRepository.findAll(paging.getLimit(), paging.getOffset()).stream()
                .map(this::toDto)
                .toList();
    }

    private Bank toEntity(BankCreateDto dto) {
        Bank bank = new Bank();
        bank.setName(dto.getName());
        bank.setBankIdentifier(dto.getBankIdentifier());
        bank.setDeleted(false);
        return bank;
    }

    private Bank toEntity(BankUpdateDto dto) {
        Bank bank = new Bank();
        bank.setName(dto.getName());
        bank.setBankIdentifier(dto.getBankIdentifier());
        bank.setDeleted(false);
        return bank;
    }

    @Override
    public BankDto create(BankCreateDto dto) {
        bankRepository.findByIdentifier(dto.getBankIdentifier())
                .orElseThrow(() -> new EntityExistsException("Already exists bank with identifier " + dto.getBankIdentifier()));
        Bank created = bankRepository.create(toEntity(dto));
        return toDto(created);
    }

    @Override
    public BankDto update(BankUpdateDto dto) {
        Bank bank = bankRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("wasn't found bank with id = " + dto.getId()));
        String identifier = dto.getBankIdentifier();
        Optional<Bank> optBank = bankRepository.findByIdentifier(identifier);
        if (optBank.isPresent() && !optBank.get().getId().equals(bank.getId())) {
            throw new EntityExistsException("Already exists bank with identifier " + identifier);
        }
        bank.setName(dto.getName());
        bank.setBankIdentifier(dto.getBankIdentifier());
        return toDto(bankRepository.update(bank));
    }

    @Override
    public void deleteById(Long id) {
        bankRepository.deleteById(id);
    }
}
