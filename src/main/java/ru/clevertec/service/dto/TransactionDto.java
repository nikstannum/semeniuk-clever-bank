package ru.clevertec.service.dto;

import java.math.BigDecimal;
import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
public class TransactionDto {
    private String fromNumber;
    private String toNumber;
    private BigDecimal amount;
    private Currency currency;
}
