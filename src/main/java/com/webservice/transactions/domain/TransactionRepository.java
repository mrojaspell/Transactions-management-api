package com.webservice.transactions.domain;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(Long id);

    List<Long> findIdsByType(String type);

    List<Long> findChildrenIds(Long parentId);
}
