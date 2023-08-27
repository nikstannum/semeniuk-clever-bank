package ru.clevertec.service.dto;

import java.util.List;
import lombok.Data;

@Data
public class ExtractDto {
    private CommonInformationDto commonInformationDto;
    private List<List<String>> moneyMovement;
}
