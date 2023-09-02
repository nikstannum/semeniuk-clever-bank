package ru.clevertec.web.command.impl.bank;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.BankService;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class DeleteBankCommand implements Command {

    private static final String URI_DIVIDER = "/";
    private final BankService bankService;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI().substring(1);
        Long id = Long.parseLong(uri.split(URI_DIVIDER)[1]);
        bankService.deleteById(id);
        return null;
    }
}
