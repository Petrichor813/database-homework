package com.volunteer.backend.dto;

import com.volunteer.backend.enums.SignupStatus;

public class AdminSignupRecordResponse {
    private Long id;
    private Long signupId;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerPhone;
    private SignupStatus status;
    private String volunteerStartTime;
    private String volunteerEndTime;
    private Integer actualHours;
    private Double points;
    private String signupTime;
    private String note;

    public AdminSignupRecordResponse(Long id, Long signupId, Long volunteerId, String volunteerName, String volunteerPhone,
            SignupStatus status, String volunteerStartTime, String volunteerEndTime, Integer actualHours,
            Double points, String signupTime, String note) {
        this.id = id;
        this.signupId = signupId;
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.volunteerPhone = volunteerPhone;
        this.status = status;
        this.volunteerStartTime = volunteerStartTime;
        this.volunteerEndTime = volunteerEndTime;
        this.actualHours = actualHours;
        this.points = points;
        this.signupTime = signupTime;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSignupId() {
        return signupId;
    }

    public void setSignupId(Long signupId) {
        this.signupId = signupId;
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

    public String getVolunteerPhone() {
        return volunteerPhone;
    }

    public void setVolunteerPhone(String volunteerPhone) {
        this.volunteerPhone = volunteerPhone;
    }

    public SignupStatus getStatus() {
        return status;
    }

    public void setStatus(SignupStatus status) {
        this.status = status;
    }

    public String getVolunteerStartTime() {
        return volunteerStartTime;
    }

    public void setVolunteerStartTime(String volunteerStartTime) {
        this.volunteerStartTime = volunteerStartTime;
    }

    public String getVolunteerEndTime() {
        return volunteerEndTime;
    }

    public void setVolunteerEndTime(String volunteerEndTime) {
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

    public String getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(String signupTime) {
        this.signupTime = signupTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
