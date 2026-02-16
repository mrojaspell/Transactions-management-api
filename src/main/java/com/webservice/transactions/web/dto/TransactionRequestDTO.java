package com.webservice.transactions.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionRequestDTO {
    
    private Double amount;
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
