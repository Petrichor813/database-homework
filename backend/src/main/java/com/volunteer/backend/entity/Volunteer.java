/**
 * 志愿者实体类
 * <p>
 * 志愿者和用户不是严格的一对一关系，普通用户不在志愿者表内
 * </p>
 */

package com.volunteer.backend.entity;

import java.time.LocalDateTime;

import com.volunteer.backend.utils.VolunteerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
// @formatter:off
@Table(
    name = "volunteer",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "name", "phone" })
    }
)
// @formatter:on
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String name; // 姓名可能不唯一

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VolunteerStatus status;

    // 审核备注
    @Column(length = 200)
    private String reviewNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime reviewTime;

    public Volunteer() {
        this.status = VolunteerStatus.REVIEWING;
        this.createTime = LocalDateTime.now();
    }

    public Volunteer(String name, String phone, Long userId) {
        this();
        this.name = name;
        this.phone = phone;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public VolunteerStatus getStatus() {
        return status;
    }

    public void setStatus(VolunteerStatus status) {
        this.status = status;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public boolean isCertified() {
        return VolunteerStatus.CERTIFIED.equals(status);
    }

    public void approve(String note) {
        this.status = VolunteerStatus.CERTIFIED;
        this.reviewNote = note;
        this.reviewTime = LocalDateTime.now();
    }

    public void reject(String note) {
        this.status = VolunteerStatus.REJECTED;
        this.reviewNote = note;
        this.reviewTime = LocalDateTime.now();
    }

    public void resetForReapply(String name, String phone) {
        this.status = VolunteerStatus.REVIEWING;
        this.reviewNote = null;
        this.reviewTime = null;
        this.name = name;
        this.phone = phone;
    }
}
