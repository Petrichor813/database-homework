package com.volunteer.backend.dto;

public class SignupRecordResponse {
    private Long id;
    private String activityTitle;
    private String activityStartTime;
    private String activityEndTime;
    private String volunteerStartTime;
    private String volunteerEndTime;
    private String status;
    private String signupTime;
    private Integer actualHours;
    private Double points;
    private String note;

    public SignupRecordResponse() {
    }

    // @formatter:off
    public SignupRecordResponse(
        Long id,
        String activityTitle,
        String activityStartTime,
        String activityEndTime,
        String volunteerStartTime,
        String volunteerEndTime,
        String status,
        String signupTime,
        Integer actualHours,
        Double points,
        String note
    ) {
        // @formatter:on
        this.id = id;
        this.activityTitle = activityTitle;
        this.activityStartTime = activityStartTime;
        this.activityEndTime = activityEndTime;
        this.volunteerStartTime = volunteerStartTime;
        this.volunteerEndTime = volunteerEndTime;
        this.status = status;
        this.signupTime = signupTime;
        this.actualHours = actualHours;
        this.points = points;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityStartTime() {
        return activityStartTime;
    }

    public void setActivityStartTime(String activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    public String getActivityEndTime() {
        return activityEndTime;
    }

    public void setActivityEndTime(String activityEndTime) {
        this.activityEndTime = activityEndTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(String signupTime) {
        this.signupTime = signupTime;
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
