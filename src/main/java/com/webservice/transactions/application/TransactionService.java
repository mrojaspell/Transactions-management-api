package com.webservice.transactions.application;

import java.util.List;

public interface TransactionService {

    void createTransaction(Long id, Double amount, String type, Long parentId);

    List<Long> getTransactionIdsByType(String type);

    Double getSum(Long transactionId);
    
}
