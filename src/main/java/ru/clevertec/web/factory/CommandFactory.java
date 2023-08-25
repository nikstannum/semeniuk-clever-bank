package ru.clevertec.web.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.factory.ServiceFactory;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.command.impl.account.DeleteAccountCommand;
import ru.clevertec.web.command.impl.account.GetAccountCommand;
import ru.clevertec.web.command.impl.account.PostAccountCommand;
import ru.clevertec.web.command.impl.account.PutAccountCommand;

public class CommandFactory {

    private final Map<String, Command> commands;
    public final static CommandFactory INSTANCE = new CommandFactory();

    private CommandFactory() {
        commands = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());

        // accounts
        commands.put("accountsGET", new GetAccountCommand(ServiceFactory.INSTANCE.getService(AccountService.class), objectMapper));
        commands.put("accountsDELETE", new DeleteAccountCommand(ServiceFactory.INSTANCE.getService(AccountService.class)));
        commands.put("accountsPOST", new PostAccountCommand(ServiceFactory.INSTANCE.getService(AccountService.class), objectMapper));
        commands.put("accountsPUT", new PutAccountCommand(ServiceFactory.INSTANCE.getService(AccountService.class), objectMapper));

    }

    public Command getCommand(String command) {
        Command instance = commands.get(command);
        if (instance == null) {
            instance = commands.get("error");
        }
        return instance;
    }
}
