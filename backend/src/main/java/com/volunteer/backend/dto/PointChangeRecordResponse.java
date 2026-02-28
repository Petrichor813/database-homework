package com.volunteer.backend.dto;

public class PointChangeRecordResponse {
    private String time;
    private String type;
    private Double amount;
    private String reason;
    private String note;

    public PointChangeRecordResponse() {
    }

    public PointChangeRecordResponse(String time, String type, Double amount, String reason, String note) {
        this.time = time;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
