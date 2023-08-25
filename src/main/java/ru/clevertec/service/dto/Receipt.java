package ru.clevertec.service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.auxiliary.Money;

@Data
public class Receipt {
    private Long receiptNumber;
    private Instant operationTime;
    private String transactionType;
    private String bankSender;
    private String bankRecipient;
    private String senderNumberAccount;
    private String recipientNumberAccount;
    private Money money;
}
