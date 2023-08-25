package ru.clevertec.service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountUpdateDto {
    private Long id;
    private String bankIdentifier;
    private BigDecimal amount;
}
