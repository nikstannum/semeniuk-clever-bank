package ru.clevertec.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.service.util.auxiliary.Money;

@ExtendWith(MockitoExtension.class)
class MoneyUtilTest {
    private static MoneyUtil util;


    @BeforeAll
    static void setUp() {
        Map<String, BigDecimal> exchangeRates = new HashMap<>();
        exchangeRates.put("USD", BigDecimal.ONE);
        exchangeRates.put("BYN", BigDecimal.valueOf(0.5));
        exchangeRates.put("EUR", BigDecimal.valueOf(2));
        exchangeRates.put("GDP", BigDecimal.valueOf(4));
        util = new MoneyUtil(exchangeRates);
    }

    @Test
    void convertFromUSDToBYNShouldReturnEquals() {
        Money money = new Money(BigDecimal.TEN, Currency.USD);
        BigDecimal actual = util.convertToTargetCurrency(money, Currency.BYN);
        Assertions.assertThat(actual).isEqualTo(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void convertFromUSDToEURShouldReturnEquals() {
        Money money = new Money(BigDecimal.TEN, Currency.USD);
        BigDecimal actual = util.convertToTargetCurrency(money, Currency.EUR);
        Assertions.assertThat(actual).isEqualTo(BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void convertFromUSDToGDPRShouldReturnEquals() {
        Money money = new Money(BigDecimal.TEN, Currency.USD);
        BigDecimal actual = util.convertToTargetCurrency(money, Currency.GDP);
        Assertions.assertThat(actual).isEqualTo(BigDecimal.valueOf(2.5).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void convertFromEURToGDPRShouldReturnEquals() {
        Money money = new Money(BigDecimal.TEN, Currency.EUR);
        BigDecimal actual = util.convertToTargetCurrency(money, Currency.GDP);
        Assertions.assertThat(actual).isEqualTo(BigDecimal.valueOf(5).setScale(2, RoundingMode.HALF_UP));
    }
}