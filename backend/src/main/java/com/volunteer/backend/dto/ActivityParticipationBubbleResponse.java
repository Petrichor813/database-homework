package com.volunteer.backend.dto;

import java.util.List;

public class ActivityParticipationBubbleResponse {
    private List<ActivityParticipationBubble> data;

    public ActivityParticipationBubbleResponse() {
    }

    public ActivityParticipationBubbleResponse(List<ActivityParticipationBubble> data) {
        this.data = data;
    }

    public List<ActivityParticipationBubble> getData() {
        return data;
    }

    public void setData(List<ActivityParticipationBubble> data) {
        this.data = data;
    }
}
