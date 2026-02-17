package com.webservice.transactions.application.exception;

public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(Long id) {
        super("Transaction with id " + id + " already exists");
    }
}
