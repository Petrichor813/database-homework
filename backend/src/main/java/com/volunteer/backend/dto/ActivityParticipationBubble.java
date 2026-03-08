package com.volunteer.backend.dto;

import java.time.LocalDateTime;

public class ActivityParticipationBubble {
    private String activityTitle;
    private LocalDateTime activityDate;
    private String activityType;
    private Integer participantCount;
    private Integer totalHours;
    private Double totalPoints;

    public ActivityParticipationBubble() {
    }

    // @formatter:off
    public ActivityParticipationBubble(
        String activityTitle,
        LocalDateTime activityDate,
        String activityType,
        Integer participantCount,
        Integer totalHours,
        Double totalPoints
    ) {
        // @formatter:on
        this.activityTitle = activityTitle;
        this.activityDate = activityDate;
        this.activityType = activityType;
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

    public LocalDateTime getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDateTime activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
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
