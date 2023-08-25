package ru.clevertec.service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TransactionData {
    private String accountNumberFrom;
    private String accountNumberTo;
    private BigDecimal amount;
}
