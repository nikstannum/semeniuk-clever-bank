package ru.clevertec.service;

import ru.clevertec.service.dto.AccountStatementCreateDto;
import ru.clevertec.service.dto.AccountStatementDto;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;

public interface TransactionService {
    ReceiptDto transfer(TransactionDto data);

    ReceiptDto topUp(TransactionDto data);

    ReceiptDto withdraw(TransactionDto data);

}
