package com.volunteer.backend.dto.response;

public class VolunteerGrowthRadarResponse {
    private String volunteerName;
    private String volunteerPhone;
    private Integer totalActivities;
    private Integer totalHours;
    private Double totalPoints;
    private Double activityParticipation;
    private Double serviceQuality;
    private Double continuity;
    private Double initiative;
    
    private Integer completedActivities;
    private Integer distinctActivityTypes;
    private Integer monthsParticipated;
    private Integer consecutiveActiveMonths;
    private Double activityCompletionRate;
    private Double onTimeCompletionRate;
    private Double earlySignupRate;
    private Double pointsPerHour;

    public VolunteerGrowthRadarResponse() {
    }

    // @formatter:off
    public VolunteerGrowthRadarResponse(
        String volunteerName,
        String volunteerPhone,
        Integer totalActivities,
        Integer totalHours,
        Double totalPoints,
        Double activityParticipation,
        Double serviceQuality,
        Double continuity,
        Double initiative,
        Integer completedActivities,
        Integer distinctActivityTypes,
        Integer monthsParticipated,
        Integer consecutiveActiveMonths,
        Double activityCompletionRate,
        Double onTimeCompletionRate,
        Double earlySignupRate,
        Double pointsPerHour
    ) {
        // @formatter:on
        this.volunteerName = volunteerName;
        this.volunteerPhone = volunteerPhone;
        this.totalActivities = totalActivities;
        this.totalHours = totalHours;
        this.totalPoints = totalPoints;
        this.activityParticipation = activityParticipation;
        this.serviceQuality = serviceQuality;
        this.continuity = continuity;
        this.initiative = initiative;
        this.completedActivities = completedActivities;
        this.distinctActivityTypes = distinctActivityTypes;
        this.monthsParticipated = monthsParticipated;
        this.consecutiveActiveMonths = consecutiveActiveMonths;
        this.activityCompletionRate = activityCompletionRate;
        this.onTimeCompletionRate = onTimeCompletionRate;
        this.earlySignupRate = earlySignupRate;
        this.pointsPerHour = pointsPerHour;
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

    public Double getActivityParticipation() {
        return activityParticipation;
    }

    public void setActivityParticipation(Double activityParticipation) {
        this.activityParticipation = activityParticipation;
    }

    public Double getServiceQuality() {
        return serviceQuality;
    }

    public void setServiceQuality(Double serviceQuality) {
        this.serviceQuality = serviceQuality;
    }

    public Double getContinuity() {
        return continuity;
    }

    public void setContinuity(Double continuity) {
        this.continuity = continuity;
    }

    public Double getInitiative() {
        return initiative;
    }

    public void setInitiative(Double initiative) {
        this.initiative = initiative;
    }

    public Integer getCompletedActivities() {
        return completedActivities;
    }

    public void setCompletedActivities(Integer completedActivities) {
        this.completedActivities = completedActivities;
    }

    public Integer getDistinctActivityTypes() {
        return distinctActivityTypes;
    }

    public void setDistinctActivityTypes(Integer distinctActivityTypes) {
        this.distinctActivityTypes = distinctActivityTypes;
    }

    public Integer getMonthsParticipated() {
        return monthsParticipated;
    }

    public void setMonthsParticipated(Integer monthsParticipated) {
        this.monthsParticipated = monthsParticipated;
    }

    public Integer getConsecutiveActiveMonths() {
        return consecutiveActiveMonths;
    }

    public void setConsecutiveActiveMonths(Integer consecutiveActiveMonths) {
        this.consecutiveActiveMonths = consecutiveActiveMonths;
    }

    public Double getActivityCompletionRate() {
        return activityCompletionRate;
    }

    public void setActivityCompletionRate(Double activityCompletionRate) {
        this.activityCompletionRate = activityCompletionRate;
    }

    public Double getOnTimeCompletionRate() {
        return onTimeCompletionRate;
    }

    public void setOnTimeCompletionRate(Double onTimeCompletionRate) {
        this.onTimeCompletionRate = onTimeCompletionRate;
    }

    public Double getEarlySignupRate() {
        return earlySignupRate;
    }

    public void setEarlySignupRate(Double earlySignupRate) {
        this.earlySignupRate = earlySignupRate;
    }

    public Double getPointsPerHour() {
        return pointsPerHour;
    }

    public void setPointsPerHour(Double pointsPerHour) {
        this.pointsPerHour = pointsPerHour;
    }
}
