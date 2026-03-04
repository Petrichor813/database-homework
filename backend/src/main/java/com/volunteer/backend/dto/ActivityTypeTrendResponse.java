package com.volunteer.backend.dto;

import java.util.List;

public class ActivityTypeTrendResponse {
    private List<String> months;
    private List<String> activityTypes;
    private List<List<Integer>> data;

    public ActivityTypeTrendResponse() {
    }

    public ActivityTypeTrendResponse(List<String> months, List<String> activityTypes, List<List<Integer>> data) {
        this.months = months;
        this.activityTypes = activityTypes;
        this.data = data;
    }

    public List<String> getMonths() {
        return months;
    }

    public void setMonths(List<String> months) {
        this.months = months;
    }

    public List<String> getActivityTypes() {
        return activityTypes;
    }

    public void setActivityTypes(List<String> activityTypes) {
        this.activityTypes = activityTypes;
    }

    public List<List<Integer>> getData() {
        return data;
    }

    public void setData(List<List<Integer>> data) {
        this.data = data;
    }
}
