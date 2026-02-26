package com.volunteer.backend.dto;

public class AdminExchangeRecordResponse {
    private Long id;
    private Long volunteerId;
    private String volunteerName;
    private String productName;
    private Long number;
    private Double totalPoints;
    private String status;
    private String orderTime;
    private String processTime;
    private String note;
    private String recvInfo;

    public AdminExchangeRecordResponse() {
    }

    public AdminExchangeRecordResponse(
        Long id,
        Long volunteerId,
        String volunteerName,
        String productName,
        Long number,
        Double totalPoints,
        String status,
        String orderTime,
        String processTime,
        String note,
        String recvInfo
    ) {
        this.id = id;
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.productName = productName;
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

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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
