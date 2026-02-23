package com.volunteer.backend.dto;

public class SignupResponse {
    private Long id; // 报名记录 ID
    private String message;

    public SignupResponse() {
    }

    public SignupResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}