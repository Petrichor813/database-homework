package com.volunteer.backend.dto;

import com.volunteer.backend.utils.UserRole;

public class RegisterRequest {
    private String username;
    private String password;
    private UserRole role;
    private String phone;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, UserRole role, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
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
}
