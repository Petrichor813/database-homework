package com.volunteer.backend.dto;

import com.volunteer.backend.enums.VolunteerReviewAction;

public class AdminVolunteerReviewRequest {
    private VolunteerReviewAction action;
    private String note;

    public AdminVolunteerReviewRequest() {
    }

    public AdminVolunteerReviewRequest(VolunteerReviewAction action, String note) {
        this.action = action;
        this.note = note;
    }

    public VolunteerReviewAction getAction() {
        return action;
    }

    public void setAction(VolunteerReviewAction action) {
        this.action = action;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}