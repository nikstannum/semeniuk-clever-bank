package ru.clevertec.web.command.impl.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.AccountService;
import ru.clevertec.service.dto.AccountDto;
import ru.clevertec.service.dto.CommonInformationDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ExtractStatementCreateDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.service.util.serializer.Serializer;
import ru.clevertec.service.util.serializer.Writable;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.PagingUtil;
import ru.clevertec.web.util.PagingUtil.Paging;

@RequiredArgsConstructor
public class GetAccountCommand implements Command {

    private static final String EXC_MSG_BAD_REQUEST = "Bad request";
    private static final String PATH_VAR_EXTRACT = "extract";
    private static final String PATH_VAR_STATEMENT = "statement";
    private static final String URI_DIVIDER = "/";
    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    private final Serializer appSerializable;
    private final Writable writable;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        try {
            return completeOperation(req);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String completeOperation(HttpServletRequest req) throws IOException {
        String uri = req.getRequestURI().substring(1);
        String[] parts = uri.split(URI_DIVIDER);
        switch (parts.length) {
            case 1 -> {
                Paging paging = PagingUtil.getPaging(req);
                return processAll(paging);
            }
            case 2 -> {
                String pathVariable = parts[1];
                switch (pathVariable.toLowerCase()) {
                    case PATH_VAR_EXTRACT -> {
                        return processExtract(req);
                    }
                    case PATH_VAR_STATEMENT -> {
                        return processStatement(req);
                    }
                    default -> {
                        return processById(parts);
                    }
                }
            }
            default -> throw new BadRequestException(EXC_MSG_BAD_REQUEST);
        }
    }

    private String processExtract(HttpServletRequest req) throws IOException {
        byte[] bytes = req.getInputStream().readAllBytes();
        ExtractStatementCreateDto dto = objectMapper.readValue(bytes, ExtractStatementCreateDto.class);
        ExtractDto result = accountService.getExtract(dto);
        printReport(appSerializable.serialize(result), result.getCommonInformationDto());
        return objectMapper.writeValueAsString(result);
    }

    private String processStatement(HttpServletRequest req) throws IOException {
        byte[] bytes = req.getInputStream().readAllBytes();
        ExtractStatementCreateDto dto = objectMapper.readValue(bytes, ExtractStatementCreateDto.class);
        StatementDto result = accountService.getMoneyStatement(dto);
        printReport(appSerializable.serialize(result), result.getCommonInformationDto());
        return objectMapper.writeValueAsString(result);
    }

    private String processAll(Paging paging) throws JsonProcessingException {
        List<AccountDto> list = accountService.findAll(paging);
        return objectMapper.writeValueAsString(list);
    }

    private String processById(String[] parts) throws JsonProcessingException {
        long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new BadRequestException(EXC_MSG_BAD_REQUEST);
        }
        AccountDto dto = accountService.findById(id);
        return objectMapper.writeValueAsString(dto);
    }

    private void printReport(String serialized, CommonInformationDto commonInf) {
        String fileName = commonInf.getFormationTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        writable.write(serialized, fileName);
    }
}
