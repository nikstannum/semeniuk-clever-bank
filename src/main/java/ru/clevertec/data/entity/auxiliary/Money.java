package ru.clevertec.data.entity.auxiliary;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.clevertec.data.entity.Currency;

@Getter
@AllArgsConstructor
public class Money {
    private Currency currency;
    private BigDecimal amount;
}
