package ru.clevertec.web.command.impl.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.UserService;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class DeleteUserCommand implements Command {
    private final UserService service;
    private static final String URI_DIVIDER = "/";

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        String uri = req.getRequestURI().substring(1);
        Long id = Long.parseLong(uri.split(URI_DIVIDER)[1]);
        service.deleteById(id);
        return null;
    }
}
