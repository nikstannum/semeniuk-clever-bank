package ru.clevertec.service.impl;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.entity.auxiliary.Money;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.dto.Receipt;
import ru.clevertec.service.dto.TransactionData;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;
import ru.clevertec.service.util.MoneyUtil;

@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Receipt transferMoney(TransactionData data) {
        Account accountFrom = getValidSenderAccount(data);
        Account accountTo = accountRepository.findByNumber(data.getAccountNumberTo()).orElseThrow(() -> new NotFoundException("invalid recipient number"));
        BigDecimal transactionAmount = data.getAmount();
        Transaction transaction = prepareTransaction(accountFrom, transactionAmount, accountTo);
        Transaction created = transactionRepository.create(transaction);
        return prepareReceipt(created, accountFrom, accountTo);
    }

    private Receipt prepareReceipt(Transaction created, Account accountFrom, Account accountTo) {
        Receipt receipt = new Receipt();
        receipt.setReceiptNumber(created.getId());
        receipt.setOperationTime(created.getTransactionTime());
        Long accountFromId = created.getAccountId();
        Long destinationAccountId = created.getDestinationAccountId();
        String transactionType;
        if (accountFromId != null && destinationAccountId != null) {
            transactionType = "transfer to " + accountTo.getUser().getLastName();
        } else if (accountFromId == null && destinationAccountId != null) {
            transactionType = "refill";
        } else {
            transactionType = "cash withdrawal";
        }
        receipt.setTransactionType(transactionType);
        receipt.setBankSender(accountFrom.getBank().getName());
        receipt.setBankRecipient(accountTo.getBank().getName());
        receipt.setSenderNumberAccount(accountFrom.getNumber());
        receipt.setRecipientNumberAccount(accountTo.getNumber());
        Money money = new Money(accountFrom.getCurrency(), created.getAccountAmount());
        receipt.setMoney(money);
        return receipt;
    }

    private Transaction prepareTransaction(Account accountFrom, BigDecimal transactionAmount, Account accountTo) {
        BigDecimal currentAmountFrom = accountFrom.getAmount();
        BigDecimal newAmountFrom = currentAmountFrom.subtract(transactionAmount);
        BigDecimal newAmountTo = getNewAmountTo(accountFrom, transactionAmount, accountTo);
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountFrom.getId());
        transaction.setDestinationAccountId(accountTo.getId());
        transaction.setAccountAmount(newAmountFrom);
        transaction.setDestinationAccountAmount(newAmountTo);
        return transaction;
    }

    private static BigDecimal getNewAmountTo(Account accountFrom, BigDecimal transactionAmount, Account accountTo) {
        Money money = new Money(accountFrom.getCurrency(), transactionAmount);
        Currency targetCurrency = accountTo.getCurrency();
        BigDecimal amountInDestinationCurrency = MoneyUtil.convertToTargetCurrency(money, targetCurrency);
        BigDecimal currentAmountTo = accountTo.getAmount();
        return currentAmountTo.add(amountInDestinationCurrency);
    }

    private Account getValidSenderAccount(TransactionData data) {
        String accountNumberFrom = data.getAccountNumberFrom();
        Account accountFrom = accountRepository.findByNumber(accountNumberFrom).orElseThrow(() -> new NotFoundException("wasn't found account with " +
                "number " + accountNumberFrom));
        BigDecimal paymentAmount = data.getAmount();
        BigDecimal accountFromAmountSize = accountFrom.getAmount();
        if (paymentAmount.compareTo(accountFromAmountSize) > 0) {
            throw new TransactionException("Not enough funds in the account");
        }
        return accountFrom;
    }
}
