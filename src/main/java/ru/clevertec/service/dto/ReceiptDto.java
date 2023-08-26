package ru.clevertec.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
@JsonInclude(Include.NON_NULL)
public class ReceiptDto {
    private Long receiptNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime operationTime;
    private String transactionType;
    private String bankSender;
    private String bankRecipient;
    private String senderNumberAccount;
    private String recipientNumberAccount;
    private BigDecimal amount;
    private Currency currency;
}
