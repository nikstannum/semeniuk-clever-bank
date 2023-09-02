package ru.clevertec.service.util.serializer.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import ru.clevertec.service.dto.CommonInformationDto;
import ru.clevertec.service.dto.ExtractDto;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.StatementDto;
import ru.clevertec.service.util.serializer.Serializer;

public class StringSerializer implements Serializer {

    private static final String EXTRACT = "Account statement";
    private static final int FIELD_WIDTH_FOR_CHECK = 45;
    private static final int STRING_LENGTH_FOR_CHECK = 43;
    private static final int FIELD_WIDTH_FOR_EXTRACT = 63;
    private static final int FIELD_DATE_EXTRACT = 11;
    private static final int FIELD_NOTICE_EXTRACT = 36;
    private static final int FIELD_AMOUNT_EXTRACT = 12;
    private static final int LEFT_FIELD_WIDTH_FOR_COMMON_INFT = 30;
    private static final int STRING_LENGTH_FOR_COMMON_INF = 60;
    private static final int RIGHT_FIELD_FOR_COMMON_INF = STRING_LENGTH_FOR_COMMON_INF - LEFT_FIELD_WIDTH_FOR_COMMON_INFT;
    private static final String HORIZON_LINE_EXTRACT = StringUtils.center("", FIELD_WIDTH_FOR_EXTRACT, "-") + "\n";
    private static final String VERT_LINE_START = "| ";
    private static final String VERT_LINE_END = " |\n";
    private static final String HORIZON_LINE_CHECK = "+" + StringUtils.center("", FIELD_WIDTH_FOR_CHECK, "-") + "+\n";
    private static final String BANK_RECEIPT = "BANK RECEIPT";
    private static final String RECEIPT = "Receipt:";
    private static final String PATTERN_DD_MM_YYYY = "dd-MM-yyyy";
    private static final String PATTERN_HH_MM_SS = "HH:mm:ss";
    private static final String TYPE_OF_TRANSACTION = "Type of transaction:";
    private static final String SENDER_S_BANK = "Sender's bank:";
    private static final String PAYEE_S_BANK = "Payee's bank:";
    private static final String SENDER_S_ACCOUNT = "Sender:";
    private static final String BENEFICIARY_S_ACCOUNT = "Benef.:";
    private static final String AMOUNT = "Amount:";

    @Override
    public String serialize(ReceiptDto dto) {
        String head = VERT_LINE_START
                + StringUtils.center(BANK_RECEIPT, STRING_LENGTH_FOR_CHECK)
                + VERT_LINE_END;
        String receipt = RECEIPT;
        String checkNum = VERT_LINE_START
                + receipt
                + StringUtils.leftPad(dto.getReceiptNumber().toString(), STRING_LENGTH_FOR_CHECK - receipt.length())
                + VERT_LINE_END;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(PATTERN_DD_MM_YYYY);
        LocalDate localDate = dto.getOperationTime().toLocalDate();
        String formattedDate = localDate.format(dateFormat);
        LocalTime localTime = dto.getOperationTime().toLocalTime();
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(PATTERN_HH_MM_SS);
        String formattedTime = localTime.format(timeFormat);
        String date = VERT_LINE_START
                + formattedDate
                + StringUtils.leftPad(formattedTime, STRING_LENGTH_FOR_CHECK - formattedDate.length())
                + VERT_LINE_END;
        String typeTransactionStr = TYPE_OF_TRANSACTION;
        String transaction = VERT_LINE_START
                + typeTransactionStr
                + StringUtils.leftPad(dto.getTransactionType(), STRING_LENGTH_FOR_CHECK - typeTransactionStr.length())
                + VERT_LINE_END;
        String bankSenderStr = SENDER_S_BANK;
        String bankSender = VERT_LINE_START
                + bankSenderStr
                + StringUtils.leftPad(dto.getBankSender(), STRING_LENGTH_FOR_CHECK - bankSenderStr.length())
                + VERT_LINE_END;
        String bankRecipientStr = PAYEE_S_BANK;
        String bankRecipient = VERT_LINE_START
                + bankRecipientStr
                + StringUtils.leftPad(dto.getBankRecipient(), STRING_LENGTH_FOR_CHECK - bankRecipientStr.length())
                + VERT_LINE_END;
        String senderAccountNumStr = SENDER_S_ACCOUNT;
        String senderAccountNum = VERT_LINE_START
                + senderAccountNumStr
                + StringUtils.leftPad(dto.getSenderNumberAccount(), STRING_LENGTH_FOR_CHECK - senderAccountNumStr.length())
                + VERT_LINE_END;
        String recipientAccountNumStr = BENEFICIARY_S_ACCOUNT;
        String recipientAccountNum = VERT_LINE_START
                + recipientAccountNumStr
                + StringUtils.leftPad(dto.getRecipientNumberAccount(), STRING_LENGTH_FOR_CHECK - recipientAccountNumStr.length())
                + VERT_LINE_END;
        String sumStr = AMOUNT;
        String sumCurrencyStr = dto.getAmount() + " " + dto.getCurrency();
        String sum = VERT_LINE_START
                + sumStr
                + StringUtils.leftPad(sumCurrencyStr, STRING_LENGTH_FOR_CHECK - sumStr.length())
                + VERT_LINE_END;

        StringBuilder result = new StringBuilder(HORIZON_LINE_CHECK + head + checkNum + date + transaction);
        if (dto.getBankSender() != null && dto.getBankRecipient() != null) {
            result.append(bankSender)
                    .append(bankRecipient)
                    .append(senderAccountNum)
                    .append(recipientAccountNum);
        }
        if (dto.getBankSender() == null) {
            result.append(bankRecipient).append(recipientAccountNum);
        }
        if (dto.getBankRecipient() == null) {
            result.append(bankSender).append(senderAccountNum);
        }
        return result.append(sum).append(HORIZON_LINE_CHECK).toString();
    }

    @Override
    public String serialize(ExtractDto extractDto) {
        CommonInformationDto commonInformationDto = extractDto.getCommonInformationDto();
        String accountCurrency = commonInformationDto.getCurrency().toString();
        String commonInf = serializeCommonInf(commonInformationDto);
        String dateHeader = StringUtils.center("Date", FIELD_DATE_EXTRACT) + VERT_LINE_START;
        String noticeHeader = StringUtils.center("Notice", FIELD_NOTICE_EXTRACT) + VERT_LINE_START;
        String amountHeader = StringUtils.center("Amount", FIELD_AMOUNT_EXTRACT) + "\n";
        String header = dateHeader + noticeHeader + amountHeader + HORIZON_LINE_EXTRACT;
        List<List<String>> movement = extractDto.getMoneyMovement();
        List<String> tableData = movement.stream()
                .map(op -> op.get(0) + " " + VERT_LINE_START
                        + StringUtils.rightPad(op.get(1), FIELD_NOTICE_EXTRACT)
                        + VERT_LINE_START + op.get(2) + " " + accountCurrency + "\n")
                .toList();
        StringBuilder result = new StringBuilder(commonInf + header);
        for (String data : tableData) {
            result.append(data);
        }
        return result.toString();
    }

    private String serializeCommonInf(CommonInformationDto dto) {
        String extract = StringUtils.center(EXTRACT, STRING_LENGTH_FOR_COMMON_INF) + "\n";
        String head = StringUtils.center(dto.getBankName(), STRING_LENGTH_FOR_COMMON_INF) + "\n";
        String clientStr = StringUtils.rightPad("Client", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String checkNum = clientStr
                + StringUtils.rightPad(VERT_LINE_START + dto.getClientFullName(), RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String accountStr = StringUtils.rightPad("Account", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String accountNum = accountStr
                + StringUtils.rightPad(VERT_LINE_START + dto.getAccountNumber(), RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String currencyStr = StringUtils.rightPad("Currency", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String currency = currencyStr
                + StringUtils.rightPad(VERT_LINE_START + dto.getCurrency(), RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String openDateStr = StringUtils.rightPad("Open date", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        DateTimeFormatter standardFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String opened = dto.getOpenTime().format(standardFormatter);
        String openDate = openDateStr
                + StringUtils.rightPad(VERT_LINE_START + opened, RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String periodStr = StringUtils.rightPad("Period", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String periodFormatted = dto.getPeriodFrom().format(standardFormatter) + " - " + dto.getPeriodTo().format(standardFormatter);
        String period = periodStr
                + StringUtils.rightPad(VERT_LINE_START + periodFormatted, RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String formationStr = StringUtils.rightPad("Date and time of formation", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        DateTimeFormatter formationFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH.mm");
        String formationFormatted = dto.getFormationTime().format(formationFormat);
        String formation = formationStr
                + StringUtils.rightPad(VERT_LINE_START + formationFormatted, RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        String balanceStr = StringUtils.rightPad("Balance", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String preparedBalance = dto.getBalance() + " " + dto.getCurrency();
        String balance = balanceStr
                + StringUtils.rightPad(VERT_LINE_START + preparedBalance, RIGHT_FIELD_FOR_COMMON_INF) + "\n";
        return extract + head + checkNum + accountNum + currency + openDate + period + formation + balance + "\n";
    }

    @Override
    public String serialize(StatementDto dto) {
        CommonInformationDto commonInformationDto = dto.getCommonInformationDto();
        String commonInf = serializeCommonInf(commonInformationDto);
        String incomeStrHead = StringUtils.center("Income", LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String expenseStrHead = StringUtils.center("Expense", RIGHT_FIELD_FOR_COMMON_INF);
        String header = incomeStrHead + "|" + expenseStrHead + "\n" + HORIZON_LINE_EXTRACT;
        Map<String, BigDecimal> incomeExpense = dto.getIncomeExpense();
        String currency = commonInformationDto.getCurrency().toString();
        String income = StringUtils.center(incomeExpense.get("income") + " " + currency, LEFT_FIELD_WIDTH_FOR_COMMON_INFT);
        String expense = StringUtils.center(incomeExpense.get("expense") + " " + currency, RIGHT_FIELD_FOR_COMMON_INF);
        return commonInf + header + income + expense;
    }
}
