package ru.clevertec.service.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.auxiliary.Money;

public class MoneyUtil {

    @SuppressWarnings("unchecked")
    public static BigDecimal convertToTargetCurrency(Money money, Currency targetCurrency) {
        Map<String, Object> exchangeRates = (Map<String, Object>) ConfigManager.INSTANCE.getProperty("exchange-rates");
        String currentCurrency = money.getCurrency().toString();
        BigDecimal currentCurrencyExchangeRateToUsd = (BigDecimal) exchangeRates.get(currentCurrency);
        BigDecimal amountInUsd = money.getAmount().multiply(currentCurrencyExchangeRateToUsd);
        String targetCurrencyStr = targetCurrency.toString();
        BigDecimal targetCurrencyExchangeRateToUsd = (BigDecimal) exchangeRates.get(targetCurrencyStr);
        return amountInUsd.divide(targetCurrencyExchangeRateToUsd, 2, RoundingMode.HALF_UP);
    }
}
