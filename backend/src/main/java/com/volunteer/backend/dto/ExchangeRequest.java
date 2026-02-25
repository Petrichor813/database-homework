package com.volunteer.backend.dto;

public class ExchangeRequest {
    private Long productId;
    private Long number;

    public ExchangeRequest() {
    }

    public ExchangeRequest(Long productId, Long number) {
        this.productId = productId;
        this.number = number;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }
}
