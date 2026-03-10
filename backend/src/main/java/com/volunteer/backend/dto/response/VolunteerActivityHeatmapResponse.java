package com.volunteer.backend.dto.response;

import java.util.List;

public class VolunteerActivityHeatmapResponse {
    private List<String> months;
    private List<String> weekdays;
    private List<List<Integer>> data;

    public VolunteerActivityHeatmapResponse() {
    }

    // @formatter:off
    public VolunteerActivityHeatmapResponse(
        List<String> months,
        List<String> weekdays,
        List<List<Integer>> data
    ) {
        // @formatter:on
        this.months = months;
        this.weekdays = weekdays;
        this.data = data;
    }

    public List<String> getMonths() {
        return months;
    }

    public void setMonths(List<String> months) {
        this.months = months;
    }

    public List<String> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(List<String> weekdays) {
        this.weekdays = weekdays;
    }

    public List<List<Integer>> getData() {
        return data;
    }

    public void setData(List<List<Integer>> data) {
        this.data = data;
    }
}
