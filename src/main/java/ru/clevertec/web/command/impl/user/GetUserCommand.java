package ru.clevertec.web.command.impl.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.UserService;
import ru.clevertec.service.dto.UserDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.PagingUtil;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class GetUserCommand implements Command {
    private static final String EXC_MSG_BAD_REQUEST = "Bad request";
    private static final String URI_DIVIDER = "/";
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        try {
            return completeOperation(req);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String completeOperation(HttpServletRequest req) throws JsonProcessingException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split(URI_DIVIDER);
        if (parts.length == 1) {
            Paging paging = PagingUtil.getPaging(req);
            return processAll(paging);
        } else {
            return processById(parts);
        }
    }

    private String processAll(Paging paging) throws JsonProcessingException {
        List<UserDto> list = userService.findAll(paging);
        return objectMapper.writeValueAsString(list);
    }

    private String processById(String[] parts) throws JsonProcessingException {
        long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new BadRequestException(EXC_MSG_BAD_REQUEST);
        }
        UserDto dto = userService.findById(id);
        return objectMapper.writeValueAsString(dto);
    }
}
