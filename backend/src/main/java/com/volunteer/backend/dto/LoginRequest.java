package com.volunteer.backend.dto;

import com.volunteer.backend.utils.UserRole;

public class LoginRequest {
    private String username;
    private String passwd;
    private UserRole role;
    
    public LoginRequest() {
    }

    public LoginRequest(String username, String passwd, UserRole role) {
        this.username = username;
        this.passwd = passwd;
        this.role = role;
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
}
