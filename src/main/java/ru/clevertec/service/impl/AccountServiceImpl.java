package ru.clevertec.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    @Override
    public AccountDto getById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found account with id = " + id));
        return toDto(account);
    }

    @Override
    public List<AccountDto> getAll(Paging paging) {
        return accountRepository.findAll(paging.getLimit(), paging.getOffset()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public AccountDto update(AccountUpdateDto dto) {
        Bank bank = bankRepository.findByIdentifier(dto.getBankIdentifier()).orElseThrow(() -> new NotFoundException("The specified bank does not " +
                "exist"));
        Account account = new Account();
        account.setId(dto.getId());
        account.setBank(bank);
        account.setAmount(dto.getAmount());
        Account updated = accountRepository.update(account);
        return toDto(updated);
    }

    @Override
    public AccountDto create(AccountCreateDto dto) {
        User user = userRepository.findUserByEmail(dto.getEmail()).orElseThrow(() -> new NotFoundException("User registration required"));
        Bank bank = bankRepository.findByIdentifier(dto.getBankIdentifier()).orElseThrow(() -> new NotFoundException("The specified bank does not " +
                "exist"));
        Account account = new Account();
        account.setUser(user);
        account.setBank(bank);
        account.setCurrency(dto.getCurrency());
        Account created = accountRepository.create(account);
        return toDto(created);
    }

    private Account toEntity(AccountDto dto) {
        Account account = new Account();
        account.setId(dto.getId());
        account.setNumber(dto.getNumber());
        account.setAmount(dto.getAmount());
        account.setCurrency(dto.getCurrency());
        account.setOpenTime(dto.getOpenTime());
        BankDto bankDto = dto.getBank();
        Bank bank = new Bank();
        bank.setId(bankDto.getId());
        bank.setName(bankDto.getName());
        bank.setBankIdentifier(bankDto.getBankIdentifier());
        account.setBank(bank);
        UserDto userDto = dto.getUser();
        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(user.getLastName());
        user.setEmail(user.getEmail());
        account.setUser(user);
        return account;
    }

    private AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setNumber(account.getNumber());
        dto.setAmount(account.getAmount());
        dto.setCurrency(account.getCurrency());
        dto.setOpenTime(account.getOpenTime());
        Bank bank = account.getBank();
        BankDto bankDto = new BankDto();
        bankDto.setId(bank.getId());
        bankDto.setName(bank.getName());
        bankDto.setBankIdentifier(bank.getBankIdentifier());
        dto.setBank(bankDto);
        User user = account.getUser();
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        dto.setUser(userDto);
        return dto;
    }
}
