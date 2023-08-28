package ru.clevertec.web.command.impl.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;
import ru.clevertec.service.exception.BadRequestException;
import ru.clevertec.web.command.Command;

@RequiredArgsConstructor
public class PostTransactionCommand implements Command {
    private static final String EXC_MSG_INVALID_INPUT_DATA = "invalid input data";
    private final TransactionService service;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        TransactionDto data;
        try {
            byte[] bytes = req.getInputStream().readAllBytes();
            data = objectMapper.readValue(bytes, TransactionDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (data.getFromNumber() != null && data.getToNumber() != null) {
            return executeTransfer(data);
        } else if (data.getFromNumber() == null && data.getToNumber() != null) {
            return executeTopUp(data);
        } else if (data.getFromNumber() != null) {
            return executeWithdrawal(data);
        } else {
            throw new BadRequestException(EXC_MSG_INVALID_INPUT_DATA);
        }
    }

    private String executeTransfer(TransactionDto data) {
        ReceiptDto receipt = service.transfer(data);
        try {
            return objectMapper.writeValueAsString(receipt);
        } catch (JsonProcessingException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    private String executeTopUp(TransactionDto data) {
        ReceiptDto receipt = service.topUp(data);
        try {
            return objectMapper.writeValueAsString(receipt);
        } catch (JsonProcessingException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }

    private String executeWithdrawal(TransactionDto data) {
        ReceiptDto receipt = service.withdraw(data);
        try {
            return objectMapper.writeValueAsString(receipt);
        } catch (JsonProcessingException e) {
            // FIXME add logging
            throw new RuntimeException(e);
        }
    }
}
