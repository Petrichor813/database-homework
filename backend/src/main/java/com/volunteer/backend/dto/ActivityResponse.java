package com.volunteer.backend.dto;

import java.time.LocalDateTime;

import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.ActivityType;

public class ActivityResponse {
    private Long id;
    private String title;
    private String description;
    private ActivityType type;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ActivityStatus status;
    private Double pointsPerHour;
    private Integer maxParticipants;
    private Integer curParticipants;

    // @formatter:off
    public ActivityResponse(
        Long id,
        String title,
        String description,
        ActivityType type,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ActivityStatus status,
        Double pointsPerHour,
        Integer maxParticipants,
        Integer curParticipants
    ) {
        // @formatter:on
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.pointsPerHour = pointsPerHour;
        this.maxParticipants = maxParticipants;
        this.curParticipants = curParticipants;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ActivityType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public Double getPointsPerHour() {
        return pointsPerHour;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getCurParticipants() {
        return curParticipants;
    }
}
