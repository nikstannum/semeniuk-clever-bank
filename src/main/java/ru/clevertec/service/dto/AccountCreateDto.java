package ru.clevertec.service.dto;

import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
public class AccountCreateDto {
    private String email;
    private String bankIdentifier;
    private Currency currency;
}
