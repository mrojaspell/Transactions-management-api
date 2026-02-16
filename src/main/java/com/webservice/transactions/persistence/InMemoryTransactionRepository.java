package com.webservice.transactions.persistence;

import org.springframework.stereotype.Repository;

import com.webservice.transactions.application.TransactionRepository;
import com.webservice.transactions.domain.Transaction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    
    private final Map<Long, Transaction> transactionsById = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> transactionsByType = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> transactionsByParentId = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    /*
        Aunque ya este usando colecciones thread safe, uso un lock para siumular
        atomicidad entre las 3 operaciones de guardado en fin de simular una BD.

        Mantengo las otras colecciones concurrentes para mantener thread safe las 
        lecturas, ya que un sistema de transacciones escribe 1 vez la factura y
        se lee multiples veces. Un sistema así tiende a ser tener muchas mas 
        lecturas por escritura.
     */ 
    

    public void save(Transaction transaction){
        synchronized(lock){
            transactionsById.put(transaction.getId(), transaction);
            transactionsByType.computeIfAbsent(transaction.getType(), k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(transaction.getId());
            if(transaction.getParentId() != null){
                transactionsByParentId.computeIfAbsent(transaction.getParentId(), k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(transaction.getId());
            }
        }
    }

    public Optional<Transaction> findById(Long id){
        Optional<Transaction> transaction = Optional.ofNullable(transactionsById.get(id));
        return transaction;
    }

    public List<Long> findIdsByType(String type){
        List<Long> ids = transactionsByType.get(type);
        return ids != null ? ids : java.util.Collections.emptyList();
    }

    public List<Long> findChildrenIds(Long parentId){
        List<Long> childIds = transactionsByParentId.get(parentId);
        return childIds != null ? childIds : java.util.Collections.emptyList();
    }
}