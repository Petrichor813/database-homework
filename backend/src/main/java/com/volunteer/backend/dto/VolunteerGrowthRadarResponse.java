package com.volunteer.backend.dto;

public class VolunteerGrowthRadarResponse {
    private String volunteerName;
    private String volunteerPhone;
    private Integer totalActivities;
    private Integer totalHours;
    private Double totalPoints;
    private Integer activityParticipation;
    private Integer serviceQuality;
    private Integer continuity;
    private Integer initiative;

    public VolunteerGrowthRadarResponse() {
    }

    public VolunteerGrowthRadarResponse(
        String volunteerName,
        String volunteerPhone,
        Integer totalActivities,
        Integer totalHours,
        Double totalPoints,
        Integer activityParticipation,
        Integer serviceQuality,
        Integer continuity,
        Integer initiative
    ) {
        this.volunteerName = volunteerName;
        this.volunteerPhone = volunteerPhone;
        this.totalActivities = totalActivities;
        this.totalHours = totalHours;
        this.totalPoints = totalPoints;
        this.activityParticipation = activityParticipation;
        this.serviceQuality = serviceQuality;
        this.continuity = continuity;
        this.initiative = initiative;
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

    public Integer getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Integer totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getActivityParticipation() {
        return activityParticipation;
    }

    public void setActivityParticipation(Integer activityParticipation) {
        this.activityParticipation = activityParticipation;
    }

    public Integer getServiceQuality() {
        return serviceQuality;
    }

    public void setServiceQuality(Integer serviceQuality) {
        this.serviceQuality = serviceQuality;
    }

    public Integer getContinuity() {
        return continuity;
    }

    public void setContinuity(Integer continuity) {
        this.continuity = continuity;
    }

    public Integer getInitiative() {
        return initiative;
    }

    public void setInitiative(Integer initiative) {
        this.initiative = initiative;
    }
}
