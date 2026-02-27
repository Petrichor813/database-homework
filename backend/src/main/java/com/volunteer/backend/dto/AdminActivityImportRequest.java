package com.volunteer.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.ActivityType;

public class AdminActivityImportRequest {
    private String title;
    private String description;
    private ActivityType type;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private ActivityStatus status;
    private Double pointsPerHour;
    private Integer maxParticipants;

    public AdminActivityImportRequest() {
    }

    // @formatter:off
    public AdminActivityImportRequest(
        String title,
        String description,
        ActivityType type,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ActivityStatus status,
        Double pointsPerHour,
        Integer maxParticipants
    ) {
        // @formatter:on
        this.title = title;
        this.description = description;
        this.type = type;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.pointsPerHour = pointsPerHour;
        this.maxParticipants = maxParticipants;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public Double getPointsPerHour() {
        return pointsPerHour;
    }

    public void setPointsPerHour(Double pointsPerHour) {
        this.pointsPerHour = pointsPerHour;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
