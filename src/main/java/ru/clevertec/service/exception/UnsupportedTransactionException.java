package ru.clevertec.service.exception;

public class UnsupportedTransactionException extends AppException {
    public UnsupportedTransactionException() {
    }

    public UnsupportedTransactionException(String message) {
        super(message);
    }

    public UnsupportedTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedTransactionException(Throwable cause) {
        super(cause);
    }
}
