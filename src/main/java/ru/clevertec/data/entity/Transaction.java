package ru.clevertec.data.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class Transaction {
    private Long id;
    private Account accountFrom;
    private Account accountTo;
    private BigDecimal accountAmountFrom;
    private BigDecimal accountAmountTo;
    private Instant transactionTime;
}
