package com.volunteer.backend.dto.request;

public class ExchangeRequest {
    private Long productId;
    private Long number;
    private String recvInfo;

    public ExchangeRequest() {
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

    public String getRecvInfo() {
        return recvInfo;
    }

    public void setRecvInfo(String recvInfo) {
        this.recvInfo = recvInfo;
    }
}
