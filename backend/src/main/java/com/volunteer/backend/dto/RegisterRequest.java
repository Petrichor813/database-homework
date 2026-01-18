package com.volunteer.backend.dto;

import com.volunteer.backend.utils.UserRole;

public class RegisterRequest {
    private String username;
    private String passwd;
    private UserRole role;
    private String phone;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String passwd, UserRole role, String phone) {
        this.username = username;
        this.passwd = passwd;
        this.role = role;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
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
