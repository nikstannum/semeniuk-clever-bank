package ru.clevertec.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.clevertec.data.DbTransactionManager;
import ru.clevertec.data.DbTransactionManagerImpl;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.BankRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.data.repository.UserRepository;
import ru.clevertec.data.repository.impl.AccountRepositoryImpl;
import ru.clevertec.data.repository.impl.BankRepositoryImpl;
import ru.clevertec.data.repository.impl.TransactionRepositoryImpl;
import ru.clevertec.data.repository.impl.UserRepositoryImpl;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.BankService;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.UserService;
import ru.clevertec.service.impl.AccountServiceImpl;
import ru.clevertec.service.impl.BankServiceImpl;
import ru.clevertec.service.impl.TransactionServiceImpl;
import ru.clevertec.service.impl.UserServiceImpl;
import ru.clevertec.service.util.MoneyUtil;
import ru.clevertec.service.util.serializer.Serializer;
import ru.clevertec.service.util.serializer.Writable;
import ru.clevertec.service.util.serializer.impl.PDFWriter;
import ru.clevertec.service.util.serializer.impl.StringSerializer;
import ru.clevertec.service.util.serializer.impl.TXTWriter;
import ru.clevertec.web.command.impl.account.DeleteAccountCommand;
import ru.clevertec.web.command.impl.account.GetAccountCommand;
import ru.clevertec.web.command.impl.account.PostAccountCommand;
import ru.clevertec.web.command.impl.account.PutAccountCommand;
import ru.clevertec.web.command.impl.bank.DeleteBankCommand;
import ru.clevertec.web.command.impl.bank.GetBankCommand;
import ru.clevertec.web.command.impl.bank.PostBankCommand;
import ru.clevertec.web.command.impl.bank.PutBankCommand;
import ru.clevertec.web.command.impl.error.ErrorCommand;
import ru.clevertec.web.command.impl.transaction.PostTransactionCommand;
import ru.clevertec.web.command.impl.user.DeleteUserCommand;
import ru.clevertec.web.command.impl.user.GetUserCommand;
import ru.clevertec.web.command.impl.user.PostUserCommand;
import ru.clevertec.web.command.impl.user.PutUserCommand;

public class BeanFactory implements Closeable {

    public final static BeanFactory INSTANCE = new BeanFactory();
    private final Map<String, Object> beans;
    private final List<Closeable> closeables;
    private ScheduledExecutorService executorService;

    private BeanFactory() {
        closeables = new ArrayList<>();
        beans = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());

        // data
        ConfigManager configManager = new ConfigManager("/application.yml");
        DataSource dataSource = new DataSource(configManager);
        closeables.add(dataSource);

        AccountRepository accountRepository = new AccountRepositoryImpl(dataSource);
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        BankRepository bankRepository = new BankRepositoryImpl(dataSource);
        TransactionRepository transactionRepository = new TransactionRepositoryImpl(dataSource);
        DbTransactionManager dbTransactionManager = new DbTransactionManagerImpl(dataSource);

        // utils
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> ratesProps = (Map<String, BigDecimal>) configManager.getProperty("exchange-rates");
        MoneyUtil moneyUtil = new MoneyUtil(ratesProps);

        // service
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> interestProps = (Map<String, BigDecimal>) configManager.getProperty("interest");
        BigDecimal percent = new BigDecimal(String.valueOf(interestProps.get("percent")));
        AccountService accountService = new AccountServiceImpl(accountRepository, userRepository, bankRepository, transactionRepository,
                dbTransactionManager, percent);
        TransactionService transactionService = new TransactionServiceImpl(transactionRepository, accountRepository, dbTransactionManager, moneyUtil);
        BigDecimal periodStr = new BigDecimal(String.valueOf(interestProps.get("periodicity")));
        long periodicity = periodStr.longValue();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(accountService::accrueInterest, 0, periodicity, TimeUnit.SECONDS);
        Serializer appSerializable = new StringSerializer();
        @SuppressWarnings("unchecked")
        Map<String, String> checkProps = (Map<String, String>) configManager.getProperty("check");
        String fontPath = checkProps.get("font-path");
        String destDir = checkProps.get("destination-dir");
        String format = checkProps.get("format");
        Writable writable;
        if ("pdf".equalsIgnoreCase(format)) {
            writable = new PDFWriter(fontPath, destDir);
        } else {
            writable = new TXTWriter(destDir);
        }
        BankService bankService = new BankServiceImpl(bankRepository);
        UserService userService = new UserServiceImpl(userRepository);

        beans.put("accountsGET", new GetAccountCommand(accountService, objectMapper, appSerializable, writable));
        beans.put("accountsDELETE", new DeleteAccountCommand(accountService));
        beans.put("accountsPOST", new PostAccountCommand(accountService, objectMapper));
        beans.put("accountsPUT", new PutAccountCommand(accountService, objectMapper));
        beans.put("banksGET", new GetBankCommand(bankService, objectMapper));
        beans.put("banksDELETE", new DeleteBankCommand(bankService));
        beans.put("banksPOST", new PostBankCommand(bankService, objectMapper));
        beans.put("banksPUT", new PutBankCommand(bankService, objectMapper));
        beans.put("usersGET", new GetUserCommand(userService, objectMapper));
        beans.put("usersDELETE", new DeleteUserCommand(userService));
        beans.put("usersPOST", new PostUserCommand(userService, objectMapper));
        beans.put("usersPUT", new PutUserCommand(userService, objectMapper));
        beans.put("transactionsPOST", new PostTransactionCommand(transactionService, objectMapper, appSerializable, writable));
        beans.put("error", new ErrorCommand(objectMapper));
    }

    public Object getBean(String command) {
        Object instance = beans.get(command);
        if (instance == null) {
            instance = beans.get("error");
        }
        return instance;
    }

    @Override
    public void close() {
        executorService.shutdown();
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
