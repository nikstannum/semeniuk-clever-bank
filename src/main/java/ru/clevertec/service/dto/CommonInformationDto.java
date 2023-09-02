package ru.clevertec.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import ru.clevertec.data.entity.Currency;

@Data
public class CommonInformationDto {
    private String bankName;
    private String clientFullName;
    private String accountNumber;
    private Currency currency;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate openTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate periodFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate periodTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy, HH.mm")
    private LocalDateTime formationTime;
    private BigDecimal balance;
}
