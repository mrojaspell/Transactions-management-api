package com.webservice.transactions.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransactionRequestDTO {
    
    @NotNull
    private Double amount;
    
    @NotBlank
    private String type;
    
    @JsonProperty("parent_id")
    private Long parentId;

    
    public Double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Long getParentId() {
        return parentId;
    }

}
