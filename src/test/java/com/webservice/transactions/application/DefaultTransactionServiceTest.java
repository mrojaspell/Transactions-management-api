package com.webservice.transactions.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.webservice.transactions.application.exception.ParentTransactionNotFoundException;
import com.webservice.transactions.application.exception.TransactionAlreadyExistsException;
import com.webservice.transactions.application.exception.TransactionNotFoundException;
import com.webservice.transactions.domain.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DefaultTransactionService transactionService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    // ── createTransaction ──────────────────────────────────────────

    @Test
    void createTransaction_savesSuccessfully() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        transactionService.createTransaction(1L, 100.0, "type1", null);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();

        assertEquals(1L, saved.getId());
        assertEquals(100.0, saved.getAmount());
        assertEquals("type1", saved.getType());
        assertNull(saved.getParentId());
    }

    @Test
    void createTransaction_duplicateId_throwsException() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));

        TransactionAlreadyExistsException ex = assertThrows(
                TransactionAlreadyExistsException.class,
                () -> transactionService.createTransaction(1L, 200.0, "type2", null)
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(null, 100.0, "type1", null));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_nullAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, null, "type1", null));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_nullType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(1L, 100.0, null, null));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_withValidParent_savesSuccessfully() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));

        transactionService.createTransaction(2L, 200.0, "type2", 1L);

        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction saved = transactionCaptor.getValue();

        assertEquals(2L, saved.getId());
        assertEquals(200.0, saved.getAmount());
        assertEquals("type2", saved.getType());
        assertEquals(1L, saved.getParentId());
    }

    @Test
    void createTransaction_withNonExistentParent_throwsException() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ParentTransactionNotFoundException ex = assertThrows(
                ParentTransactionNotFoundException.class,
                () -> transactionService.createTransaction(2L, 200.0, "type2", 99L)
        );

        assertTrue(ex.getMessage().contains("Parent"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_parentNotFound_throwsParentTransactionNotFoundException() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ParentTransactionNotFoundException.class,
                () -> transactionService.createTransaction(2L, 100.0, "type", 99L));
    }

    // ── getTransactionIdsByType ────────────────────────────────────

    @Test
    void getTransactionIdsByType_returnsMatchingIds() {
        when(transactionRepository.findIdsByType("cars"))
                .thenReturn(List.of(1L, 3L, 5L));

        List<Long> result = transactionService.getTransactionIdsByType("cars");

        assertEquals(List.of(1L, 3L, 5L), result);
        verify(transactionRepository).findIdsByType("cars");
    }

    @Test
    void getTransactionIdsByType_noMatches_returnsEmptyList() {
        when(transactionRepository.findIdsByType("unknown"))
                .thenReturn(Collections.emptyList());

        List<Long> result = transactionService.getTransactionIdsByType("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactionIdsByType_nullType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.getTransactionIdsByType(null));
    }

    // ── getSum ─────────────────────────────────────────────────────

    @Test
    void getSum_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.getSum(null));
    }

    @Test
    void getSum_singleTransaction_returnsItsAmount() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));
        when(transactionRepository.findChildrenIds(1L))
                .thenReturn(Collections.emptyList());

        Double sum = transactionService.getSum(1L);

        assertEquals(100.0, sum);
    }

    @Test
    void getSum_withDirectChildren_returnsSumOfAll() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));
        when(transactionRepository.findById(2L))
                .thenReturn(Optional.of(new Transaction(2L, 50.0, "type2", 1L)));
        when(transactionRepository.findById(3L))
                .thenReturn(Optional.of(new Transaction(3L, 25.0, "type1", 1L)));

        when(transactionRepository.findChildrenIds(1L))
                .thenReturn(List.of(2L, 3L));
        when(transactionRepository.findChildrenIds(2L))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.findChildrenIds(3L))
                .thenReturn(Collections.emptyList());

        Double sum = transactionService.getSum(1L);

        assertEquals(175.0, sum);
    }

    @Test
    void getSum_withNestedChildren_returnsSumOfAll() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));
        when(transactionRepository.findById(2L))
                .thenReturn(Optional.of(new Transaction(2L, 50.0, "type2", 1L)));
        when(transactionRepository.findById(3L))
                .thenReturn(Optional.of(new Transaction(3L, 25.0, "type1", 2L)));

        when(transactionRepository.findChildrenIds(1L)).thenReturn(List.of(2L));
        when(transactionRepository.findChildrenIds(2L)).thenReturn(List.of(3L));
        when(transactionRepository.findChildrenIds(3L)).thenReturn(Collections.emptyList());

        Double sum = transactionService.getSum(1L);

        assertEquals(175.0, sum);
    }

    @Test
    void getSum_childIdWithoutEntity_throwsException() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(new Transaction(1L, 100.0, "type1", null)));
        when(transactionRepository.findChildrenIds(1L))
                .thenReturn(List.of(2L));
        when(transactionRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getSum(1L));
    }

    @Test
    void getSum_nonExistentTransaction_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getSum(99L));
    }
}
