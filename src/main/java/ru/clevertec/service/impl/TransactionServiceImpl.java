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
import ru.clevertec.logging.Loggable;
import ru.clevertec.service.TransactionService;
import ru.clevertec.service.dto.ReceiptDto;
import ru.clevertec.service.dto.TransactionDto;
import ru.clevertec.service.exception.NotFoundException;
import ru.clevertec.service.exception.TransactionException;
import ru.clevertec.service.util.MoneyUtil;
import ru.clevertec.service.util.auxiliary.Money;

@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final String EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER = "wasn't found account with number ";
    private static final String EXC_MSG_NOT_FOUND_RECIPIENT_NUMBER = "invalid recipient number ";
    private static final String TRANSACTION_TRANSFER = "transfer";
    private static final String TRANSACTION_TOP_UP = "top up";
    private static final String TRANSACTION_WITHDRAWAL = "withdrawal";
    private static final String UNKNOWN_TRANSACTION = "Unknown transaction";
    private static final String EXC_MSG_NOT_ENOUGH_FUNDS_IN_THE_ACCOUNT = "Not enough funds in the account";
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DbTransactionManager dbTransactionManager;
    private final MoneyUtil moneyUtil;

    /**
     * Method for making a transaction between two users
     *
     * @param data for the operation
     * @return the result of the transaction with a description of the accounts, the amount in the currency of the sender's account,
     * the time of the transaction, etc.
     */
    @Override
    @Loggable
    public ReceiptDto transfer(TransactionDto data) {
        Account accountFrom = accountRepository.findByNumber(data.getFromNumber())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER + data.getFromNumber()));
        Account accountTo = accountRepository.findByNumber(data.getToNumber())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_RECIPIENT_NUMBER + data.getToNumber()));
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

    /**
     * Method that implements the logic of replenishing an account with cash
     *
     * @param data for the operation
     * @return The result of the transaction indicating the account number, currency amounts, etc.
     */
    @Override
    @Loggable
    public ReceiptDto topUp(TransactionDto data) {
        Account accountTo = accountRepository.findByNumber(data.getToNumber())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_RECIPIENT_NUMBER + data.getToNumber()));
        Money transactionMoney = getTransactionMoney(data);
        Transaction transaction = prepareTransaction(null, transactionMoney, accountTo);
        dbTransactionManager.execute(connection -> {
            accountRepository.updateAmountByNumber(accountTo, connection);
            transactionRepository.createTransaction(transaction, connection);
        });
        return prepareReceipt(transaction);
    }

    /**
     * The method that implements the logic of withdrawing cash from the account.
     *
     * @param data for the operation
     * @return The result of the transaction indicating the account number, currency amounts, etc.
     */
    @Override
    @Loggable
    public ReceiptDto withdraw(TransactionDto data) {
        Account accountFrom = accountRepository.findByNumber(data.getFromNumber())
                .orElseThrow(() -> new NotFoundException(EXC_MSG_NOT_FOUND_ACCOUNT_BY_NUMBER + data.getFromNumber()));
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
            transaction.setAccountAmountFrom(transactionAmountInDestinationCurrency);
        }
        if (accountTo != null) {
            Currency currencyTo = accountTo.getCurrency();
            BigDecimal transactionAmountInDestinationCurrency = moneyUtil.convertToTargetCurrency(transactionMoney, currencyTo);
            BigDecimal newAmountAccountTo = accountTo.getAmount().add(transactionAmountInDestinationCurrency);
            accountTo.setAmount(newAmountAccountTo);
            transaction.setAccountTo(accountTo);
            transaction.setAccountAmountTo(transactionAmountInDestinationCurrency);
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
            transactionType = TRANSACTION_TRANSFER;
            receipt.setCurrency(accountFrom.getCurrency());
            receipt.setAmount(created.getAccountAmountFrom());
            receipt.setBankSender(accountFrom.getBank().getName());
            receipt.setBankRecipient(accountTo.getBank().getName());
            receipt.setSenderNumberAccount(accountFrom.getNumber());
            receipt.setRecipientNumberAccount(accountTo.getNumber());
        } else if (accountFrom == null && accountTo != null) {
            transactionType = TRANSACTION_TOP_UP;
            receipt.setCurrency(accountTo.getCurrency());
            receipt.setAmount(created.getAccountAmountTo());
            receipt.setBankRecipient(accountTo.getBank().getName());
            receipt.setRecipientNumberAccount(accountTo.getNumber());
        } else if (accountFrom != null) {
            transactionType = TRANSACTION_WITHDRAWAL;
            receipt.setCurrency(accountFrom.getCurrency());
            receipt.setAmount(created.getAccountAmountFrom());
            receipt.setBankSender(accountFrom.getBank().getName());
            receipt.setSenderNumberAccount(accountFrom.getNumber());
        } else {
            throw new RuntimeException(UNKNOWN_TRANSACTION);
        }
        receipt.setTransactionType(transactionType);
        return receipt;
    }

    private void validateOperation(Account accountFrom, TransactionDto data) {
        BigDecimal paymentAmount = data.getAmount();
        BigDecimal accountFromAmountSize = accountFrom.getAmount();
        if (paymentAmount.compareTo(accountFromAmountSize) > 0) {
            throw new TransactionException(EXC_MSG_NOT_ENOUGH_FUNDS_IN_THE_ACCOUNT);
        }
    }
}
