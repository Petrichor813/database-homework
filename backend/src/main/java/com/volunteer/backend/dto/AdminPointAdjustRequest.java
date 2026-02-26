package com.volunteer.backend.dto;

import com.volunteer.backend.enums.PointChangeType;

public class AdminPointAdjustRequest {
    private Long volunteerId;
    private Double changePoints;
    private PointChangeType changeType;
    private String reason;
    private String note;

    public AdminPointAdjustRequest() {
    }

    public AdminPointAdjustRequest(
        Long volunteerId,
        Double changePoints,
        PointChangeType changeType,
        String reason,
        String note
    ) {
        this.volunteerId = volunteerId;
        this.changePoints = changePoints;
        this.changeType = changeType;
        this.reason = reason;
        this.note = note;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public Double getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(Double changePoints) {
        this.changePoints = changePoints;
    }

    public PointChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PointChangeType changeType) {
        this.changeType = changeType;
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
