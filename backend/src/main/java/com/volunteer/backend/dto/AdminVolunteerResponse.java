package com.volunteer.backend.dto;

import java.time.LocalDateTime;

import com.volunteer.backend.utils.VolunteerStatus;

public class AdminVolunteerResponse {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private VolunteerStatus status;
    private String reviewNote;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;

    public AdminVolunteerResponse() {
    }

    // @formatter:off
    public AdminVolunteerResponse(
        Long id,
        Long userId,
        String name,
        String phone,
        VolunteerStatus status,
        String reviewNote,
        LocalDateTime createTime,
        LocalDateTime reviewTime
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.reviewNote = reviewNote;
        this.createTime = createTime;
        this.reviewTime = reviewTime;
    }
    // @formatter:on

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
}