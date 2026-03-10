package com.volunteer.backend.dto.response;

public class DashboardKPIResponse {
    private Double totalServiceHours;
    private Integer totalActivities;
    private Integer activeVolunteers;
    private Double totalPointsIssued;

    public DashboardKPIResponse() {
    }

    // @formatter:off
    public DashboardKPIResponse(
        Double totalServiceHours,
        Integer totalActivities,
        Integer activeVolunteers,
        Double totalPointsIssued
    ) {
        // @formatter:on
        this.totalServiceHours = totalServiceHours;
        this.totalActivities = totalActivities;
        this.activeVolunteers = activeVolunteers;
        this.totalPointsIssued = totalPointsIssued;
    }

    public Double getTotalServiceHours() {
        return totalServiceHours;
    }

    public void setTotalServiceHours(Double totalServiceHours) {
        this.totalServiceHours = totalServiceHours;
    }

    public Integer getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Integer totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Integer getActiveVolunteers() {
        return activeVolunteers;
    }

    public void setActiveVolunteers(Integer activeVolunteers) {
        this.activeVolunteers = activeVolunteers;
    }

    public Double getTotalPointsIssued() {
        return totalPointsIssued;
    }

    public void setTotalPointsIssued(Double totalPointsIssued) {
        this.totalPointsIssued = totalPointsIssued;
    }
}
