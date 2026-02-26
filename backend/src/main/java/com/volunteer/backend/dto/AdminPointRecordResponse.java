package com.volunteer.backend.dto;

import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.enums.RelatedRecordType;

public class AdminPointRecordResponse {
    private Long id;
    private Long volunteerId;
    private String volunteerName;
    private PointChangeType changeType;
    private Double changePoints;
    private Double balanceAfter;
    private String reason;
    private String note;
    private RelatedRecordType relatedRecordType;
    private Long relatedRecordId;
    private String changeTime;

    public AdminPointRecordResponse() {
    }

    public AdminPointRecordResponse(
        Long id,
        Long volunteerId,
        String volunteerName,
        PointChangeType changeType,
        Double changePoints,
        Double balanceAfter,
        String reason,
        String note,
        RelatedRecordType relatedRecordType,
        Long relatedRecordId,
        String changeTime
    ) {
        this.id = id;
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.changeType = changeType;
        this.changePoints = changePoints;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.note = note;
        this.relatedRecordType = relatedRecordType;
        this.relatedRecordId = relatedRecordId;
        this.changeTime = changeTime;
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

    public PointChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PointChangeType changeType) {
        this.changeType = changeType;
    }

    public Double getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(Double changePoints) {
        this.changePoints = changePoints;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
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

    public RelatedRecordType getRelatedRecordType() {
        return relatedRecordType;
    }

    public void setRelatedRecordType(RelatedRecordType relatedRecordType) {
        this.relatedRecordType = relatedRecordType;
    }

    public Long getRelatedRecordId() {
        return relatedRecordId;
    }

    public void setRelatedRecordId(Long relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public String getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(String changeTime) {
        this.changeTime = changeTime;
    }
}
