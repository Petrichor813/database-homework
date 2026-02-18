package com.volunteer.backend.dto;

import com.volunteer.backend.enums.UserRole;

public class RegisterRequest {
    private String username;
    private String password;
    private UserRole role;
    private String phone;
    private boolean requestVolunteer;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, UserRole role, String phone, boolean requestVolunteer) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.requestVolunteer = requestVolunteer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean isRequestVolunteer() {
        return requestVolunteer;
    }

    public void setRequestVolunteer(boolean requestVolunteer) {
        this.requestVolunteer = requestVolunteer;
    }
}
