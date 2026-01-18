package com.volunteer.backend.dto;

import com.volunteer.backend.utils.UserRole;

public class LoginResponse {
    private Long id;
    private String username;
    private UserRole role;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(Long id, String username, UserRole role, String token) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
