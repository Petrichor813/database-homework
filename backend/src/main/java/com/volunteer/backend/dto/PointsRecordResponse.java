package com.volunteer.backend.dto;

public class PointsRecordResponse {
    private String time;
    private String type;
    private Double amount;
    private String note;

    public PointsRecordResponse() {
    }

    public PointsRecordResponse(String time, String type, Double amount, String note) {
        this.time = time;
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
