package ru.clevertec.data.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class Account {
    private Long id;
    private String number;
    private User user;
    private Bank bank;
    private BigDecimal amount;
    private Currency currency;
    private Date openTime;
}
