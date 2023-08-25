package ru.clevertec.data.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class Transaction {
    private Long id;
    private Long accountId;
    private Long destinationAccountId;
    private BigDecimal accountAmount;
    private BigDecimal destinationAccountAmount;
    private Instant transactionTime;
}
