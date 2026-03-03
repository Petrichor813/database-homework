package com.volunteer.backend.dto;

public class AdminExchangeUpdateRequest {
    private Long number;
    private String status;

    public AdminExchangeUpdateRequest() {
    }

    public AdminExchangeUpdateRequest(Long number, String status) {
        this.number = number;
        this.status = status;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
