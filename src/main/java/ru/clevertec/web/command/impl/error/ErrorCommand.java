package ru.clevertec.web.command.impl.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.dto.ErrorDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class ErrorCommand implements Command {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_CONFLICT = 409;
    private static final int CODE_INTERNAL_SERVER_ERROR = 500;
    private static final String MSG_SERVER_ERROR = "Server error";
    private static final String MSG_CLIENT_ERROR = "Client error";
    private static final String DEFAULT_MSG = "Unknown error";
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        Exception e = (Exception) req.getAttribute("exc");
        if (e instanceof NotFoundException) {
            ErrorDto errorDto = new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
            res.setStatus(CODE_NOT_FOUND);
            return getString(errorDto);
        } else if (e instanceof BadRequestException) {
            ErrorDto errorDto = new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
            res.setStatus(CODE_BAD_REQUEST);
            return getString(errorDto);
        } else if (e instanceof TransactionException) {
            ErrorDto dto = new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
            res.setStatus(CODE_CONFLICT);
            return getString(dto);
        } else {
            ErrorDto errorDto = new ErrorDto(MSG_SERVER_ERROR, DEFAULT_MSG);
            res.setStatus(CODE_INTERNAL_SERVER_ERROR);
            return getString(errorDto);
        }
    }

    private String getString(ErrorDto errorDto) {
        try {
            return objectMapper.writeValueAsString(errorDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
