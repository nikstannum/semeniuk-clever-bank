package ru.clevertec.service.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.auxiliary.Money;

@RequiredArgsConstructor
public class MoneyUtil {

    private final Map<String, BigDecimal> exchangeRates;

    public BigDecimal convertToTargetCurrency(Money money, Currency targetCurrency) {
        String currentCurrency = money.getCurrency().toString();
        BigDecimal currentCurrencyExchangeRateToUsd = new BigDecimal(String.valueOf(exchangeRates.get(currentCurrency)));
        BigDecimal amountInUsd = money.getAmount().multiply(currentCurrencyExchangeRateToUsd);
        String targetCurrencyStr = targetCurrency.toString();
        BigDecimal targetCurrencyExchangeRateToUsd = new BigDecimal(String.valueOf(exchangeRates.get(targetCurrencyStr)));
        return amountInUsd.divide(targetCurrencyExchangeRateToUsd, 2, RoundingMode.HALF_UP);
    }
}
