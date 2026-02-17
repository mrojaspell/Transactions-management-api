package com.webservice.transactions.application.exception;

public class ParentTransactionNotFoundException extends RuntimeException{

    public ParentTransactionNotFoundException(Long parentId) {
        super("Parent transaction with id " + parentId + " does not exist");
    }
    
}
