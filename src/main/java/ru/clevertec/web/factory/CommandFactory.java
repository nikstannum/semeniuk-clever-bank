package ru.clevertec.web.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.impl.AccountServiceImpl;
import ru.clevertec.service.impl.TransactionServiceImpl;
import ru.clevertec.service.util.MoneyUtil;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.command.impl.account.DeleteAccountCommand;
import ru.clevertec.web.command.impl.account.GetAccountCommand;
import ru.clevertec.web.command.impl.account.PostAccountCommand;
import ru.clevertec.web.command.impl.account.PutAccountCommand;
import ru.clevertec.web.command.impl.transaction.PostTransactionCommand;

public class CommandFactory implements Closeable {

    public final static CommandFactory INSTANCE = new CommandFactory();
    private final Map<String, Command> commands;
    private final List<Closeable> closeables;

    private CommandFactory() {
        closeables = new ArrayList<>();
        commands = new HashMap<>();
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

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> rates = (Map<String, BigDecimal>) configManager.getProperty("exchange-rates");
        MoneyUtil moneyUtil = new MoneyUtil(rates);

        // service
        AccountService accountService = new AccountServiceImpl(accountRepository, userRepository, bankRepository, transactionRepository);
        TransactionService transactionService = new TransactionServiceImpl(transactionRepository, accountRepository, dbTransactionManager, moneyUtil);

        commands.put("accountsGET", new GetAccountCommand(accountService, objectMapper));
        commands.put("accountsDELETE", new DeleteAccountCommand(accountService));
        commands.put("accountsPOST", new PostAccountCommand(accountService, objectMapper));
        commands.put("accountsPUT", new PutAccountCommand(accountService, objectMapper));
        commands.put("transactionsPOST", new PostTransactionCommand(transactionService, objectMapper));

    }

    public Command getCommand(String command) {
        Command instance = commands.get(command);
        if (instance == null) {
            instance = commands.get("error");
        }
        return instance;
    }

    @Override
    public void close() {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                // FIXME add logging
            }
        }
    }
}
