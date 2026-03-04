package com.volunteer.backend.dto;

public class ActivityParticipationBubble {
    private String activityTitle;
    private Integer participantCount;
    private Integer totalHours;
    private Double totalPoints;

    public ActivityParticipationBubble() {
    }

    public ActivityParticipationBubble(String activityTitle, Integer participantCount, Integer totalHours, Double totalPoints) {
        this.activityTitle = activityTitle;
        this.participantCount = participantCount;
        this.totalHours = totalHours;
        this.totalPoints = totalPoints;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
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
}
