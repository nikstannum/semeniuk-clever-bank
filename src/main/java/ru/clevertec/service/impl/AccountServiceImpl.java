package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.dto.BankDto;
import ru.clevertec.service.dto.CommonInformationDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final TransactionRepository transactionRepository;

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

    @Override
    public ExtractDto getExtract(ExtractStatementCreateDto createDto) {
        String number = createDto.getAccountNumber();
        Account account = accountRepository.findByNumber(number).orElseThrow(() -> new NotFoundException("account wasn't found"));
        Long id = account.getId();
        LocalDate dateFrom = createDto.getPeriodFrom();
        Instant instantFrom = dateFrom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        LocalDate dateTo = createDto.getPeriodTo();
        Instant instantTo = dateTo.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        List<Transaction> transactions = transactionRepository.findAllTransactionsForUser(instantFrom, instantTo, id);
        CommonInformationDto commonInformation = getCommonInformation(createDto, account);
        ExtractDto result = new ExtractDto();
        result.setCommonInformationDto(commonInformation);
        List<List<String>> moneyMovement = collectMoneyMovement(transactions, id);
        result.setMoneyMovement(moneyMovement);
        return result;
    }

    @Override
    public StatementDto getMoneyStatement(ExtractStatementCreateDto createDto) {
        String number = createDto.getAccountNumber();
        Account account = accountRepository.findByNumber(number).orElseThrow(() -> new NotFoundException("account wasn't found"));
        Long id = account.getId();
        LocalDate dateFrom = createDto.getPeriodFrom();
        Instant instantFrom = dateFrom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        LocalDate dateTo = createDto.getPeriodTo();
        Instant instantTo = dateTo.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        StatementDto result = new StatementDto();
        CommonInformationDto commonInformation = getCommonInformation(createDto, account);
        result.setCommonInformationDto(commonInformation);
        Map<String, BigDecimal> incomeExpenseMap = transactionRepository.findIncomeAndExpenseForUser(instantFrom, instantTo, id);
        result.setIncomeExpense(incomeExpenseMap);
        return result;
    }

    private CommonInformationDto getCommonInformation(ExtractStatementCreateDto createDto, Account account) {
        CommonInformationDto dto = new CommonInformationDto();
        dto.setBankName(account.getBank().getName());
        dto.setClientFullName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        dto.setAccountNumber(account.getNumber());
        dto.setCurrency(account.getCurrency());
        dto.setOpenTime(account.getOpenTime());
        dto.setPeriodFrom(createDto.getPeriodFrom());
        dto.setPeriodTo(createDto.getPeriodTo());
        dto.setFormationTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setBalance(account.getAmount());
        return dto;
    }

    private List<List<String>> collectMoneyMovement(List<Transaction> transactions, Long accountId) {
        return transactions.stream()
                .map(transaction -> {
                    List<String> list = new ArrayList<>();
                    addFormattedTime(transaction, list);
                    Long idFrom = transaction.getAccountFrom().getId();
                    BigDecimal moneyFrom = transaction.getAccountFromAmount();
                    BigDecimal moneyTo = transaction.getAccountToAmount();
                    String transactionType;
                    BigDecimal amount;
                    if (moneyFrom != null && moneyTo != null) {
                        if (accountId.equals(idFrom)) {
                            amount = transaction.getAccountFromAmount().negate();
                            transactionType = "transfer to " + transaction.getAccountTo().getUser().getLastName();
                        } else {
                            amount = transaction.getAccountToAmount();
                            transactionType = "replenishment from " + transaction.getAccountFrom().getUser().getLastName();
                        }
                    } else if (moneyFrom == null && moneyTo != null) {
                        amount = transaction.getAccountToAmount();
                        transactionType = "replenishment";
                    } else if (moneyFrom != null) {
                        amount = transaction.getAccountFromAmount().negate();
                        transactionType = "cash withdrawal";
                    } else {
                        throw new RuntimeException("Unknown transaction");
                    }
                    list.add(transactionType);
                    list.add(amount.toString());
                    return list;
                }).toList();
    }

    private void addFormattedTime(Transaction transaction, List<String> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate time = transaction.getTransactionTime().atZone(ZoneId.systemDefault()).toLocalDate();
        String formattedDate = time.format(formatter);
        list.add(formattedDate);
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
