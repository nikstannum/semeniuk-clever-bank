package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.DbTransactionManager;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Bank;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.entity.User;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.logging.Loggable;
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

    private static final long ZERO_ACCOUNTS_INTEREST_ACCRUAL = 0L;
    private static final int LIMIT_DEFAULT = 10;
    private static final int PERCENT_100 = 100;
    private static final int ROUNDING_ACCURACY = 2;
    private static final String EXC_MSG_NOT_FOUND_ACCOUNT_BY_ID = "Not found account with id = ";
    private static final String EXC_MSG_NOT_FOUND_BANK_BY_IDENTIFIER = "The specified bank does not exist";
    private static final String EXC_MSG_USER_REGISTRATION_REQUIRED = "User registration required";
    private static final String EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER = "Wasn't found account with number ";
    private static final String TRANSACTION_TRANSFER_TO = "transfer to ";
    private static final String TRANSACTION_REPLENISHMENT_FROM = "replenishment from ";
    private static final String TRANSACTION_REPLENISHMENT = "replenishment";
    private static final String TRANSACTION_CASH_WITHDRAWAL = "cash withdrawal";
    private static final String UNKNOWN_TRANSACTION = "Unknown transaction";
    private static final String DATE_FORMAT_DD_MM_YYYY = "dd.MM.yyyy";
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final TransactionRepository transactionRepository;
    private final DbTransactionManager dbTransactionManager;
    private final BigDecimal percent;

    @Override
    @Loggable
    public void accrueInterest() {
        dbTransactionManager.execute(connection -> {
                    Long count = accountRepository.countAccountWithAmountMoreZero(connection);
                    if (count.equals(ZERO_ACCOUNTS_INTEREST_ACCRUAL)) {
                        return;
                    }
                    int limit = count < LIMIT_DEFAULT ? count.intValue() : LIMIT_DEFAULT;
                    long iterationsQuantity = count / LIMIT_DEFAULT + 1;
                    for (long i = 0; i < iterationsQuantity; i++) {
                        long offset = LIMIT_DEFAULT * i;
                        List<Account> list = accountRepository.findAllAmountMoreZero(limit, offset, connection);
                        for (Account account : list) {
                            BigDecimal currentAmount = account.getAmount();
                            BigDecimal accrualAmount = currentAmount
                                    .multiply(percent)
                                    .divide(BigDecimal.valueOf(PERCENT_100), ROUNDING_ACCURACY, RoundingMode.HALF_UP);
                            BigDecimal newAmount = currentAmount.add(accrualAmount);
                            account.setAmount(newAmount);
                            accountRepository.updateAmountByNumber(account, connection);
                            Transaction transaction = new Transaction();
                            transaction.setAccountTo(account);
                            transaction.setAccountAmountTo(accrualAmount);
                            transactionRepository.createTransaction(transaction, connection);
                        }
                    }
                }
        );
    }

    @Override
    @Loggable
    public AccountDto findById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_ACCOUNT_BY_ID + id));
        return toDto(account);
    }

    @Override
    @Loggable
    public List<AccountDto> findAll(Paging paging) {
        return accountRepository.findAll(paging.getLimit(), paging.getOffset()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Loggable
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    @Loggable
    public AccountDto update(AccountUpdateDto dto) {
        Bank bank = bankRepository.findByIdentifier(dto.getBankIdentifier())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_BANK_BY_IDENTIFIER));
        Account account = accountRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("wasn't found account with id = " + dto.getId()));
        account.setBank(bank);
        account.setAmount(dto.getAmount());
        Account updated = accountRepository.update(account);
        return toDto(updated);
    }

    @Override
    @Loggable
    public AccountDto create(AccountCreateDto dto) {
        User user = userRepository.findUserByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_USER_REGISTRATION_REQUIRED));
        Bank bank = bankRepository.findByIdentifier(dto.getBankIdentifier())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_BANK_BY_IDENTIFIER));
        Account account = new Account();
        account.setUser(user);
        account.setBank(bank);
        account.setAmount(BigDecimal.ZERO);
        account.setCurrency(dto.getCurrency());
        Account created = accountRepository.create(account);
        return toDto(created);
    }

    @Override
    @Loggable
    public ExtractDto getExtract(ExtractStatementCreateDto createDto) {
        String number = createDto.getAccountNumber();
        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER + number));
        Long id = account.getId();
        LocalDate dateFrom = createDto.getPeriodFrom();
        Instant instantFrom = dateFrom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        LocalDate dateTo = createDto.getPeriodTo();
        Instant instantTo = dateTo.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        List<Transaction> transactions = transactionRepository.findAllTransactionsForUser(instantFrom, instantTo, id);
        CommonInformationDto commonInformation = getCommonInformation(createDto, account);
        ExtractDto result = new ExtractDto();
        result.setCommonInformationDto(commonInformation);
        List<List<String>> moneyMovement = collectMoneyMovement(transactions, id);
        result.setMoneyMovement(moneyMovement);
        return result;
    }

    @Override
    @Loggable
    public StatementDto getMoneyStatement(ExtractStatementCreateDto createDto) {
        String number = createDto.getAccountNumber();
        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER + number));
        Long id = account.getId();
        LocalDate dateFrom = createDto.getPeriodFrom();
        Instant instantFrom = dateFrom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        LocalDate dateTo = createDto.getPeriodTo();
        Instant instantTo = dateTo.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        StatementDto result = new StatementDto();
        CommonInformationDto commonInformation = getCommonInformation(createDto, account);
        result.setCommonInformationDto(commonInformation);
        Map<String, BigDecimal> incomeExpenseMap = transactionRepository.findIncomeAndExpenseForUser(instantFrom, instantTo, id);
        incomeExpenseMap.put("expense", incomeExpenseMap.get("expense").negate());
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
                    BigDecimal moneyFrom = transaction.getAccountAmountFrom();
                    BigDecimal moneyTo = transaction.getAccountAmountTo();
                    String transactionType;
                    BigDecimal amount;
                    if (moneyFrom != null && moneyTo != null) {
                        if (accountId.equals(idFrom)) {
                            amount = transaction.getAccountAmountFrom().negate();
                            transactionType = TRANSACTION_TRANSFER_TO + transaction.getAccountTo().getUser().getLastName();
                        } else {
                            amount = transaction.getAccountAmountTo();
                            transactionType = TRANSACTION_REPLENISHMENT_FROM + transaction.getAccountFrom().getUser().getLastName();
                        }
                    } else if (moneyFrom == null && moneyTo != null) {
                        amount = transaction.getAccountAmountTo();
                        transactionType = TRANSACTION_REPLENISHMENT;
                    } else if (moneyFrom != null) {
                        amount = transaction.getAccountAmountFrom().negate();
                        transactionType = TRANSACTION_CASH_WITHDRAWAL;
                    } else {
                        throw new RuntimeException(UNKNOWN_TRANSACTION);
                    }
                    list.add(transactionType);
                    list.add(amount.toString());
                    return list;
                }).toList();
    }

    private void addFormattedTime(Transaction transaction, List<String> list) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);
        LocalDate time = transaction.getTransactionTime().atZone(ZoneId.systemDefault()).toLocalDate();
        String formattedDate = time.format(formatter);
        list.add(formattedDate);
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
