package ru.clevertec.web.command.impl.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.PagingUtil;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class GetAccountCommand implements Command {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req) {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split("/");
        try {
            if (parts.length > 1) {
                return processById(parts);
            } else {
                Paging paging = PagingUtil.getPaging(req);
                return processAll(paging);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e.getCause()); // FIXME add logging
        }
    }

    private String processAll(Paging paging) throws JsonProcessingException {
        List<AccountDto> list =accountService.getAll(paging);
        return objectMapper.writeValueAsString(list);
    }

    private String processById(String[] parts) throws JsonProcessingException {
        Long id = Long.parseLong(parts[1]);
        AccountDto dto = accountService.getById(id);
        return objectMapper.writeValueAsString(dto);
    }
}
