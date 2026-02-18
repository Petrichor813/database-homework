/**
 * 报名记录实体类
 */

package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.enums.SignupStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "signup_record")
public class SignupRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long volunteerId;

    @Column(nullable = false)
    private Long activityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignupStatus status;

    // 实际服务时长（这个要由管理员在活动后决定）
    private Integer actualHours;

    // 积分，这个也是在活动后计算
    private Double points;

    @Column(nullable = false)
    private LocalDateTime signupTime;

    private LocalDateTime updateTime;

    // 备注
    @Column(length = 200)
    private String note;

    public SignupRecord() {
        this.status = SignupStatus.REVIEWING;
        this.signupTime = LocalDateTime.now();
    }

    public SignupRecord(Long volunteerId, Long activityId) {
        this();
        this.volunteerId = volunteerId;
        this.activityId = activityId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public SignupStatus getStatus() {
        return status;
    }

    public void setStatus(SignupStatus status) {
        this.status = status;
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

    public LocalDateTime getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(LocalDateTime createTime) {
        this.signupTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
