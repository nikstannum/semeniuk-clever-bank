package ru.clevertec.service.util.auxiliary;

import java.math.BigDecimal;
import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
public class Money {
    private final BigDecimal amount;
    private final Currency currency;
}
