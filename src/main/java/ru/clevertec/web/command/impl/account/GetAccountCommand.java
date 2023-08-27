package ru.clevertec.web.command.impl.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.PagingUtil;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class GetAccountCommand implements Command {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req) {
        try {
            return completeOperation(req);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        }
    }

    private String completeOperation(HttpServletRequest req) throws IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        switch (parts.length) {
            case 1 -> {
                Paging paging = PagingUtil.getPaging(req);
                return processAll(paging);
            }
            case 2 -> {
                String pathVariable = parts[1];
                switch (pathVariable.toLowerCase()) {
                    case "extract" -> {
                        return processExtract(req);
                    }
                    case "statement" -> {
                        return processStatement(req);
                    }
                    default -> {
                        return processById(parts);
                    }
                }
            }
            default -> throw new RuntimeException("Bad request");
        }
    }

    private String processExtract(HttpServletRequest req) throws IOException {
        byte[] bytes = req.getInputStream().readAllBytes();
        ExtractStatementCreateDto dto = objectMapper.readValue(bytes, ExtractStatementCreateDto.class);
        ExtractDto result = accountService.getExtract(dto);
        return objectMapper.writeValueAsString(result);
    }

    private String processStatement(HttpServletRequest req) throws IOException {
        byte[] bytes = req.getInputStream().readAllBytes();
        ExtractStatementCreateDto dto = objectMapper.readValue(bytes, ExtractStatementCreateDto.class);
        StatementDto result = accountService.getMoneyStatement(dto);
        return objectMapper.writeValueAsString(result);
    }


    private String processAll(Paging paging) throws JsonProcessingException {
        List<AccountDto> list = accountService.getAll(paging);
        return objectMapper.writeValueAsString(list);
    }

    private String processById(String[] parts) throws JsonProcessingException {
        Long id = Long.parseLong(parts[1]);
        AccountDto dto = accountService.getById(id);
        return objectMapper.writeValueAsString(dto);
    }
}
