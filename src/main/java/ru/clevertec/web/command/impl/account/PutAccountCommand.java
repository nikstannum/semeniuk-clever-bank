package ru.clevertec.web.command.impl.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountUpdateDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PutAccountCommand implements Command {
    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req) {
        String idStr = req.getRequestURI().substring(1).split("/")[1];
        Long id = Long.parseLong(idStr);
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            AccountUpdateDto dto = objectMapper.readValue(bytes, AccountUpdateDto.class);
            if (!id.equals(dto.getId())) {
                throw new BadRequestException("Incoming id in body doesn't match path");
            }
            return objectMapper.writeValueAsString(accountService.update(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
            // FIXME add logging
        }
    }
}
