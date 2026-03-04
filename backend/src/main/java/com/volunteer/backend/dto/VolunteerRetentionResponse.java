package com.volunteer.backend.dto;

import java.util.List;

public class VolunteerRetentionResponse {
    private List<String> months;
    private List<Double> retentionRates;

    public VolunteerRetentionResponse() {
    }

    public VolunteerRetentionResponse(List<String> months, List<Double> retentionRates) {
        this.months = months;
        this.retentionRates = retentionRates;
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
}
