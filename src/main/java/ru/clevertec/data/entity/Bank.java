package ru.clevertec.data.entity;

import lombok.Data;

@Data
public class Bank {
    private Long id;
    private String name;
    private String bankIdentifier;
    private boolean deleted;
}
