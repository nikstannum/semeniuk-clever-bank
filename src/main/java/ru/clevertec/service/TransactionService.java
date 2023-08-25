package ru.clevertec.service;

import ru.clevertec.service.dto.Receipt;
import ru.clevertec.service.dto.TransactionData;

public interface TransactionService {
    Receipt transferMoney(TransactionData data);
}
