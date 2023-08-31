package ru.clevertec.service.util.serializer;

import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.StatementDto;

public interface Serializer {

    <T> T serialize(ReceiptDto receiptDto);

    <T> T serialize(ExtractDto extractDto);

    <T> T serialize(StatementDto statementDto);
}
