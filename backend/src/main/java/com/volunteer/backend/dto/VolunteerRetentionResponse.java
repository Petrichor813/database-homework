package com.volunteer.backend.dto;

import java.util.List;

public class VolunteerRetentionResponse {
    private List<String> months;
    private List<Double> retentionRates;
    private List<Integer> activeVolunteers;
    private List<Integer> retainedVolunteers;
    private List<Integer> lostVolunteers;

    public VolunteerRetentionResponse() {
    }

    // @formatter:off
    public VolunteerRetentionResponse(
        List<String> months,
        List<Double> retentionRates,
        List<Integer> activeVolunteers,
        List<Integer> retainedVolunteers,
        List<Integer> lostVolunteers
    ) {
        // @formatter:on
        this.months = months;
        this.retentionRates = retentionRates;
        this.activeVolunteers = activeVolunteers;
        this.retainedVolunteers = retainedVolunteers;
        this.lostVolunteers = lostVolunteers;
    }

    public List<String> getMonths() {
        return months;
    }

    public void setMonths(List<String> months) {
        this.months = months;
    }

    public List<Double> getRetentionRates() {
        return retentionRates;
    }

    public void setRetentionRates(List<Double> retentionRates) {
        this.retentionRates = retentionRates;
    }

    public List<Integer> getActiveVolunteers() {
        return activeVolunteers;
    }

    public void setActiveVolunteers(List<Integer> activeVolunteers) {
        this.activeVolunteers = activeVolunteers;
    }

    public List<Integer> getRetainedVolunteers() {
        return retainedVolunteers;
    }

    public void setRetainedVolunteers(List<Integer> retainedVolunteers) {
        this.retainedVolunteers = retainedVolunteers;
    }

    public List<Integer> getLostVolunteers() {
        return lostVolunteers;
    }

    public void setLostVolunteers(List<Integer> lostVolunteers) {
        this.lostVolunteers = lostVolunteers;
    }
}
