package ru.clevertec.web.command.impl.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class DeleteAccountCommand implements Command {
    private static final String URI_DIVIDER = "/";
    private final AccountService service;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI().substring(1);
        Long id = Long.parseLong(uri.split(URI_DIVIDER)[1]);
        service.delete(id);
        return null;
    }
}
