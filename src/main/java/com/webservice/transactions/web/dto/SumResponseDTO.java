package com.webservice.transactions.web.dto;

public class SumResponseDTO {
    
    private final Double sum;

    public SumResponseDTO(double sum) {
        this.sum = sum;
    }

    public double getSum() {
        return sum;
    }

}
