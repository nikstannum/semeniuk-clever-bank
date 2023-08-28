package ru.clevertec.web.command.impl.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountCreateDto;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PostAccountCommand implements Command {
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            AccountCreateDto dto = objectMapper.readValue(bytes, AccountCreateDto.class);
            return objectMapper.writeValueAsString(accountService.create(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
