/**
 * 积分变动细节存储
 */

package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.utils.PointsChangeType;
import com.volunteer.backend.utils.RelatedRecordType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "points_change_record")
public class PointsChangeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long volunteerId;

    @Column(nullable = false)
    private Double changePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointsChangeType changeType;

    @Column(nullable = false, length = 200)
    private String reason;

    // 关联的业务记录 ID（报名记录的 ID 或者是兑换记录的 ID）
    private Long relatedRecordId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RelatedRecordType relatedRecordType;

    // 变动后的积分余额
    private Double balanceAfter;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changeTime;

    @Column(nullable = false, length = 200)
    private String note;

    public PointsChangeRecord() {
        this.changeTime = LocalDateTime.now();
    }

    // @formatter:off
    // 不带关联记录
    public PointsChangeRecord(
        Long volunteerId,
        Double changePoints,
        PointsChangeType changeType,
        String reason
    ) {
        this();
        this.volunteerId = volunteerId;
        this.changePoints = changePoints;
        this.changeType = changeType;
        this.reason = reason;
    }

    // 带关联记录
    public PointsChangeRecord(
        Long volunteerId,
        Double changePoints,
        PointsChangeType changeType,
        String reason,
        Long relatedRecordId,
        RelatedRecordType relatedRecordType
    ) {
        this();
        this.volunteerId = volunteerId;
        this.changePoints = changePoints;
        this.changeType = changeType;
        this.reason = reason;
        this.relatedRecordId = relatedRecordId;
        this.relatedRecordType = relatedRecordType;
    }
    // @formatter:on

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

    public Double getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(Double changePoints) {
        this.changePoints = changePoints;
    }

    public PointsChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PointsChangeType changeType) {
        this.changeType = changeType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRelatedRecordId() {
        return relatedRecordId;
    }

    public void setRelatedRecordId(Long relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public RelatedRecordType getRelatedRecordType() {
        return relatedRecordType;
    }

    public void setRelatedRecordType(RelatedRecordType relatedRecordType) {
        this.relatedRecordType = relatedRecordType;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRelatedToSignup() {
        return RelatedRecordType.SIGNUP.equals(this.relatedRecordType);
    }

    public boolean isRelatedToExchange() {
        return RelatedRecordType.EXCHANGE.equals(this.relatedRecordType);
    }

    public boolean isEarn() {
        return changePoints != null && changePoints > 0;
    }

    public boolean isDeduct() {
        return changePoints != null && changePoints < 0;
    }
}
