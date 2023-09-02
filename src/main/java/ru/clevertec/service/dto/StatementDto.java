package ru.clevertec.service.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

@Data
public class StatementDto {
    private CommonInformationDto commonInformationDto;
    private Map<String, BigDecimal> incomeExpense;
}
