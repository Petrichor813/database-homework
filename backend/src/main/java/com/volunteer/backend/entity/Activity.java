/**
 * 活动实体类
 */

package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.utils.ActivityStatus;
import com.volunteer.backend.utils.ActivityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType type;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @Column(nullable = false)
    private Double pointsPerHour;

    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer curParticipants;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    public Activity() {
        this.curParticipants = 0;
        this.createTime = LocalDateTime.now();
    }

    // @formatter:off
    public Activity(
        String title,
        ActivityType type,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double pointsPerHour,
        Integer maxParticipants
    ) {
        this();
        this.title = title;
        this.type = type;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pointsPerHour = pointsPerHour;
        this.maxParticipants = maxParticipants;
        this.status = ActivityStatus.RECRUITING;
    }
    // @formatter:on

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getCurParticipants() {
        return curParticipants;
    }

    public void setCurParticipants(Integer curParticipants) {
        this.curParticipants = curParticipants;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
