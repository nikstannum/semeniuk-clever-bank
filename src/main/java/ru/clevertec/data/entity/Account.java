package ru.clevertec.data.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Account {
    private Long id;
    private String number;
    private User user;
    private Bank bank;
    private BigDecimal amount;
    private Currency currency;
    private LocalDate openTime;
    private boolean deleted;
}
