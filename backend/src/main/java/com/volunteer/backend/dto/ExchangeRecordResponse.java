package com.volunteer.backend.dto;

public class ExchangeRecordResponse {
    private Long id;
    private String itemName;
    private Long number;
    private Double totalPoints;
    private String status;
    private String orderTime;
    private String processTime;
    private String note;
    private String recvInfo;

    public ExchangeRecordResponse() {
    }

    // @formatter:off
    public ExchangeRecordResponse(
        Long id,
        String itemName,
        Long number,
        Double totalPoints,
        String status,
        String orderTime,
        String processTime,
        String note,
        String recvInfo
    ) {
        // @formatter:on
        this.id = id;
        this.itemName = itemName;
        this.number = number;
        this.totalPoints = totalPoints;
        this.status = status;
        this.orderTime = orderTime;
        this.processTime = processTime;
        this.note = note;
        this.recvInfo = recvInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getProcessTime() {
        return processTime;
    }

    public void setProcessTime(String processTime) {
        this.processTime = processTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRecvInfo() {
        return recvInfo;
    }

    public void setRecvInfo(String recvInfo) {
        this.recvInfo = recvInfo;
    }
}
