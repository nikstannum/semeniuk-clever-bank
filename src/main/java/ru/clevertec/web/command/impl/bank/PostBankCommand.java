package ru.clevertec.web.command.impl.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.BankService;
import ru.clevertec.service.dto.BankCreateDto;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PostBankCommand implements Command {
    private final BankService bankService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            BankCreateDto dto = objectMapper.readValue(bytes, BankCreateDto.class);
            return objectMapper.writeValueAsString(bankService.create(dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
