package com.volunteer.backend.dto;

public class UpdateUserProfileRequest {
    private String username;
    private String phone;

    public UpdateUserProfileRequest() {
    }

    public UpdateUserProfileRequest(String username, String phone) {
        this.username = username;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
