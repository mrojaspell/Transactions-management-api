package com.webservice.transactions.infrastructure.web;

import com.webservice.transactions.application.TransactionService;
import com.webservice.transactions.application.exception.ParentTransactionNotFoundException;
import com.webservice.transactions.application.exception.TransactionAlreadyExistsException;
import com.webservice.transactions.application.exception.TransactionNotFoundException;
import com.webservice.transactions.infrastructure.web.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TransactionController.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Nested
    @DisplayName("PUT /transactions/{transactionId}")
    class CreateTransaction {

        @Test
        @DisplayName("should create transaction and return status ok")
        void shouldCreateTransaction() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 100.0, "type": "cars", "parent_id": null}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"status\":\"ok\"}"));

            verify(transactionService).createTransaction(1L, 100.0, "cars", null);
        }

        @Test
        @DisplayName("should create transaction with parent_id")
        void shouldCreateTransactionWithParentId() throws Exception {
            mockMvc.perform(put("/transactions/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 250.5, "type": "electronics", "parent_id": 1}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"status\":\"ok\"}"));

            verify(transactionService).createTransaction(2L, 250.5, "electronics", 1L);
        }

        @Test
        @DisplayName("should return 400 when amount is missing")
        void shouldReturn400WhenAmountMissing() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"type": "cars"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.amount").exists());

            verify(transactionService, never()).createTransaction(anyLong(), any(), anyString(), any());
        }

        @Test
        @DisplayName("should return 400 when type is blank")
        void shouldReturn400WhenTypeIsBlank() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 100.0, "type": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").exists());

            verify(transactionService, never()).createTransaction(anyLong(), any(), anyString(), any());
        }

        @Test
        @DisplayName("should return 400 when type is missing")
        void shouldReturn400WhenTypeMissing() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 100.0}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").exists());

            verify(transactionService, never()).createTransaction(anyLong(), any(), anyString(), any());
        }

        @Test
        @DisplayName("should return 400 when body is empty")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(anyLong(), any(), anyString(), any());
        }

        @Test
        @DisplayName("should return 409 when transaction already exists")
        void shouldReturn409WhenTransactionAlreadyExists() throws Exception {
            doThrow(new TransactionAlreadyExistsException(1L))
                    .when(transactionService).createTransaction(eq(1L), anyDouble(), anyString(), any());

            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 100.0, "type": "cars"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Transaction with id 1 already exists"));
        }

        @Test
        @DisplayName("should return 400 when parent transaction does not exist")
        void shouldReturn400WhenParentNotFound() throws Exception {
            doThrow(new ParentTransactionNotFoundException(99L))
                    .when(transactionService).createTransaction(eq(2L), anyDouble(), anyString(), eq(99L));

            mockMvc.perform(put("/transactions/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 50.0, "type": "cars", "parent_id": 99}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Parent transaction with id 99 does not exist"));
        }
    }

    @Nested
    @DisplayName("GET /transactions/types/{type}")
    class GetByType {

        @Test
        @DisplayName("should return list of transaction ids for a given type")
        void shouldReturnTransactionIdsByType() throws Exception {
            when(transactionService.getTransactionIdsByType("cars")).thenReturn(List.of(1L, 3L, 5L));

            mockMvc.perform(get("/transactions/types/cars"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[1, 3, 5]"));
        }

        @Test
        @DisplayName("should return empty list when no transactions match the type")
        void shouldReturnEmptyListWhenNoMatch() throws Exception {
            when(transactionService.getTransactionIdsByType("unknown")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/transactions/types/unknown"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        @DisplayName("should return error when type is null literal")
        void shouldReturnErrorWhenTypeIsNull() throws Exception {
            when(transactionService.getTransactionIdsByType("null"))
                    .thenThrow(new IllegalArgumentException("Type must not be null"));

            mockMvc.perform(get("/transactions/types/null"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Type must not be null"));
        }
    }

    @Nested
    @DisplayName("GET /transactions/sum/{transactionId}")
    class GetSum {

        @Test
        @DisplayName("should return sum for a given transaction")
        void shouldReturnSum() throws Exception {
            when(transactionService.getSum(1L)).thenReturn(350.5);

            mockMvc.perform(get("/transactions/sum/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(350.5));
        }

        @Test
        @DisplayName("should return sum of zero")
        void shouldReturnZeroSum() throws Exception {
            when(transactionService.getSum(1L)).thenReturn(0.0);

            mockMvc.perform(get("/transactions/sum/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(0.0));
        }

        @Test
        @DisplayName("should return 404 when transaction not found")
        void shouldReturn404WhenTransactionNotFound() throws Exception {
            when(transactionService.getSum(99L)).thenThrow(new TransactionNotFoundException(99L));

            mockMvc.perform(get("/transactions/sum/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Transaction with id 99 not found"));
        }
    }
}
