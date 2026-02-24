package com.volunteer.backend.dto;

import java.time.LocalDateTime;

import com.volunteer.backend.enums.SignupStatus;

public class AdminSignupUpdateRequest {
    private SignupStatus status;
    private LocalDateTime volunteerStartTime;
    private LocalDateTime volunteerEndTime;
    private Integer actualHours;
    private Double points;
    private String note;

    public SignupStatus getStatus() {
        return status;
    }

    public void setStatus(SignupStatus status) {
        this.status = status;
    }

    public LocalDateTime getVolunteerStartTime() {
        return volunteerStartTime;
    }

    public void setVolunteerStartTime(LocalDateTime volunteerStartTime) {
        this.volunteerStartTime = volunteerStartTime;
    }

    public LocalDateTime getVolunteerEndTime() {
        return volunteerEndTime;
    }

    public void setVolunteerEndTime(LocalDateTime volunteerEndTime) {
        this.volunteerEndTime = volunteerEndTime;
    }

    public Integer getActualHours() {
        return actualHours;
    }

    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
