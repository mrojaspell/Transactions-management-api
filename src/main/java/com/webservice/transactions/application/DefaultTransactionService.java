package com.webservice.transactions.application;

import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Service;

import com.webservice.transactions.application.exception.ParentTransactionNotFoundException;
import com.webservice.transactions.application.exception.TransactionAlreadyExistsException;
import com.webservice.transactions.application.exception.TransactionNotFoundException;
import com.webservice.transactions.domain.Transaction;

@Service
public class DefaultTransactionService implements TransactionService {
    
    private final TransactionRepository transactionRepository;

    public DefaultTransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void createTransaction(Long id, Double amount, String type, Long parentId){
        
        if(id == null || amount == null || type == null){
            throw new IllegalArgumentException("Transaction properties cannot be null");
        }

        if(transactionRepository.findById(id).isPresent()){
            throw new TransactionAlreadyExistsException(id);
        }

        if(parentId != null && transactionRepository.findById(parentId).isEmpty()){
            throw new ParentTransactionNotFoundException(parentId);
        }

        if(parentId != null && parentId.equals(id)){
            throw new IllegalArgumentException("Transaction cannot be its own parent");
        }
        
        Transaction transaction = new Transaction(id, amount, type, parentId);
        
        transactionRepository.save(transaction);
    }

    public List<Long> getTransactionIdsByType(String type){
        if(type == null){
            throw new IllegalArgumentException("Type cannot be null");
        }

        return transactionRepository.findIdsByType(type);
    }

    public Double getSum(Long transactionId){ 
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        if (transactionRepository.findById(transactionId).isEmpty()) {
            throw new TransactionNotFoundException(transactionId);
        }

        Deque<Long> stack = new java.util.ArrayDeque<>();
        stack.push(transactionId);

        double sum = 0.0;

        while(!stack.isEmpty()) {
            Long currentId = stack.pop();
            Transaction transaction = transactionRepository.findById(currentId).orElseThrow(() -> new TransactionNotFoundException(currentId));

            sum += transaction.getAmount();

            for(Long childId : transactionRepository.findChildrenIds(currentId)) {
                stack.push(childId);
            }
        }
        return sum;
    }

}
