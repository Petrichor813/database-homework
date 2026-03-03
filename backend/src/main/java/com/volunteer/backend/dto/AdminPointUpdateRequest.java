package com.volunteer.backend.dto;

public class AdminPointUpdateRequest {
    private Double changePoints;
    private String reason;
    private String note;

    public AdminPointUpdateRequest() {
    }

    public Double getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(Double changePoints) {
        this.changePoints = changePoints;
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
