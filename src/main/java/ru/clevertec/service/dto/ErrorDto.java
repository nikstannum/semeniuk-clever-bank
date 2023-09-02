package ru.clevertec.service.dto;

import lombok.Data;

@Data
public class ErrorDto {
    private final String type;
    private final String message;
}
