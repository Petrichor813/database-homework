package com.volunteer.backend.dto;

public class SignupRequest {
    private Long activityId;
    private String volunteerStartTime;
    private String volunteerEndTime;

    public SignupRequest() {
    }

    public SignupRequest(Long activityId, String volunteerStartTime, String volunteerEndTime) {
        this.activityId = activityId;
        this.volunteerStartTime = volunteerStartTime;
        this.volunteerEndTime = volunteerEndTime;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
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
}
