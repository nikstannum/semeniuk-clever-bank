package ru.clevertec.service.dto;

import lombok.Data;

@Data
public class BankDto {
    private Long id;
    private String name;
    private String bankIdentifier;
}
