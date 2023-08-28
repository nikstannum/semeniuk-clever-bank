package ru.clevertec.service.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.DbTransactionManager;
import ru.clevertec.data.entity.Account;
import ru.clevertec.data.entity.Currency;
import ru.clevertec.data.entity.Transaction;
import ru.clevertec.data.repository.AccountRepository;
import ru.clevertec.data.repository.TransactionRepository;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;
import ru.clevertec.service.util.MoneyUtil;
import ru.clevertec.service.util.auxiliary.Money;

@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DbTransactionManager dbTransactionManager;
    private final MoneyUtil moneyUtil;

    @Override
    public ReceiptDto transfer(TransactionDto data) {
        Account accountFrom = accountRepository.findByNumber(data.getFromNumber())
                .orElseThrow(() -> new NotFoundException("wasn't found account with number " + data.getFromNumber()));
        Account accountTo = accountRepository.findByNumber(data.getToNumber())
                .orElseThrow(() -> new NotFoundException("invalid recipient number"));
        Money transactionMoney = getTransactionMoney(data);
        Transaction transaction = prepareTransaction(accountFrom, transactionMoney, accountTo);
        dbTransactionManager.execute(connection -> {
            validateOperation(accountFrom, data);
            accountRepository.updateAmountByNumber(accountFrom, connection);
            accountRepository.updateAmountByNumber(accountTo, connection);
            transactionRepository.createTransaction(transaction, connection);
        });
        return prepareReceipt(transaction);
    }

    @Override
    public ReceiptDto topUp(TransactionDto data) {
        Account accountTo = accountRepository.findByNumber(data.getToNumber()).orElseThrow(() -> new NotFoundException("invalid recipient number"));
        Money transactionMoney = getTransactionMoney(data);
        Transaction transaction = prepareTransaction(null, transactionMoney, accountTo);
        dbTransactionManager.execute(connection -> {
            accountRepository.updateAmountByNumber(accountTo, connection);
            transactionRepository.createTransaction(transaction, connection);
        });
        return prepareReceipt(transaction);
    }

    @Override
    public ReceiptDto withdraw(TransactionDto data) {
        Account accountFrom = accountRepository.findByNumber(data.getFromNumber())
                .orElseThrow(() -> new NotFoundException("wasn't found account with number " + data.getFromNumber()));
        Money transactionMoney = getTransactionMoney(data);
        Transaction transaction = prepareTransaction(accountFrom, transactionMoney, null);
        dbTransactionManager.execute(connection -> {
            validateOperation(accountFrom, data);
            accountRepository.updateAmountByNumber(accountFrom, connection);
            transactionRepository.createTransaction(transaction, connection);
        });
        return prepareReceipt(transaction);
    }

    private Money getTransactionMoney(TransactionDto data) {
        BigDecimal transactionAmount = data.getAmount();
        Currency currency = data.getCurrency();
        return new Money(transactionAmount, currency);
    }

    private Transaction prepareTransaction(Account accountFrom, Money transactionMoney, Account accountTo) {
        Transaction transaction = new Transaction();
        if (accountFrom != null) {
            Currency currencyFrom = accountFrom.getCurrency();
            BigDecimal transactionAmountInDestinationCurrency = moneyUtil.convertToTargetCurrency(transactionMoney, currencyFrom);
            BigDecimal newAmountAccountFrom = accountFrom.getAmount().subtract(transactionAmountInDestinationCurrency);
            accountFrom.setAmount(newAmountAccountFrom);
            transaction.setAccountFrom(accountFrom);
            transaction.setAccountFromAmount(transactionAmountInDestinationCurrency);
        }
        if (accountTo != null) {
            Currency currencyTo = accountTo.getCurrency();
            BigDecimal transactionAmountInDestinationCurrency = moneyUtil.convertToTargetCurrency(transactionMoney, currencyTo);
            BigDecimal newAmountAccountTo = accountTo.getAmount().add(transactionAmountInDestinationCurrency);
            accountTo.setAmount(newAmountAccountTo);
            transaction.setAccountTo(accountTo);
            transaction.setAccountToAmount(transactionAmountInDestinationCurrency);
        }
        return transaction;
    }

    private ReceiptDto prepareReceipt(Transaction created) {
        ReceiptDto receipt = new ReceiptDto();
        receipt.setReceiptNumber(created.getId());
        receipt.setOperationTime(created.getTransactionTime().atZone(ZoneId.systemDefault()).toLocalDateTime());
        Account accountFrom = created.getAccountFrom();
        Account accountTo = created.getAccountTo();
        String transactionType;
        if (accountFrom != null && accountTo != null) {
            transactionType = "transfer";
            receipt.setCurrency(accountFrom.getCurrency());
            receipt.setAmount(created.getAccountFromAmount());
            receipt.setBankSender(accountFrom.getBank().getName());
            receipt.setBankRecipient(accountTo.getBank().getName());
            receipt.setSenderNumberAccount(accountFrom.getNumber());
            receipt.setRecipientNumberAccount(accountTo.getNumber());
        } else if (accountFrom == null && accountTo != null) {
            transactionType = "top up";
            receipt.setCurrency(accountTo.getCurrency());
            receipt.setAmount(created.getAccountToAmount());
            receipt.setBankRecipient(accountTo.getBank().getName());
            receipt.setRecipientNumberAccount(accountTo.getNumber());
        } else if (accountFrom != null) {
            transactionType = "withdrawal";
            receipt.setCurrency(accountFrom.getCurrency());
            receipt.setAmount(created.getAccountFromAmount());
            receipt.setBankSender(accountFrom.getBank().getName());
            receipt.setSenderNumberAccount(accountFrom.getNumber());
        } else {
            throw new RuntimeException("Unknown transaction");
        }
        receipt.setTransactionType(transactionType);
        return receipt;
    }

    private void validateOperation(Account accountFrom, TransactionDto data) {
        BigDecimal paymentAmount = data.getAmount();
        BigDecimal accountFromAmountSize = accountFrom.getAmount();
        if (paymentAmount.compareTo(accountFromAmountSize) > 0) {
            throw new TransactionException("Not enough funds in the account");
        }
    }
}
