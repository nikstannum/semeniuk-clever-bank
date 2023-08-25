package ru.clevertec.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
public class AccountDto {
    private Long id;
    private String number;
    private UserDto user;
    private BankDto bank;
    private BigDecimal amount;
    private Currency currency;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate openTime;
}
