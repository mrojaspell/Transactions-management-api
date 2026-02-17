package com.webservice.transactions.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webservice.transactions.application.TransactionService;
import com.webservice.transactions.web.dto.TransactionRequestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.webservice.transactions.web.dto.SumResponseDTO;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;



import java.util.List;

@RestController
@RequestMapping("/transactions")
@Validated
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<String> putMethodName(@PathVariable @NotNull Long transactionId, @Valid @RequestBody TransactionRequestDTO request) {
        
        transactionService.createTransaction(
            transactionId,
            request.getAmount(),
            request.getType(),
            request.getParentId()
        );
        
        return ResponseEntity.ok("{\"status\":\"ok\"}");
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable @NotBlank String type) {
        return ResponseEntity.ok(transactionService.getTransactionIdsByType(type));
    }

    
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponseDTO> getSum(@PathVariable @NotNull Long transactionId) {
        double sum = transactionService.getSum(transactionId);
        return ResponseEntity.ok(new SumResponseDTO(sum));
    }

}
