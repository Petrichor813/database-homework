package com.volunteer.backend.dto;

import java.util.List;

import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerStatus;

public class UserProfileResponse {
    private Long id;
    private String username;
    private UserRole role;
    private String phone;
    private String realName;
    private VolunteerStatus volunteerStatus;
    private String applyReason;
    private Double points;
    private Integer serviceHours;
    private List<PointChangeRecordResponse> pointsRecords;

    public UserProfileResponse() {
    }

    // @formatter:off
    public UserProfileResponse(
        Long id,
        String username,
        UserRole role,
        String phone,
        String realName,
        VolunteerStatus volunteerStatus,
        String applyReason,
        Double points,
        Integer serviceHours,
        List<PointChangeRecordResponse> pointsRecords
    ) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.phone = phone;
        this.realName = realName;
        this.volunteerStatus = volunteerStatus;
        this.applyReason = applyReason;
        this.points = points;
        this.serviceHours = serviceHours;
        this.pointsRecords = pointsRecords;
    }
    // @formatter:on

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public VolunteerStatus getVolunteerStatus() {
        return volunteerStatus;
    }

    public void setVolunteerStatus(VolunteerStatus volunteerStatus) {
        this.volunteerStatus = volunteerStatus;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Integer getServiceHours() {
        return serviceHours;
    }

    public void setServiceHours(Integer serviceHours) {
        this.serviceHours = serviceHours;
    }

    public List<PointChangeRecordResponse> getPointsRecords() {
        return pointsRecords;
    }

    public void setPointsRecords(List<PointChangeRecordResponse> pointsRecords) {
        this.pointsRecords = pointsRecords;
    }
}