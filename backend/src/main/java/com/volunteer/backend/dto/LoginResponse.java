package com.volunteer.backend.dto;

import com.volunteer.backend.enums.UserRole;
import com.volunteer.backend.enums.VolunteerStatus;

public class LoginResponse {
    private Long id;
    private String username;
    private UserRole role;
    private String token;
    private VolunteerStatus volunteerStatus;
    private String phone;

    public LoginResponse() {
    }

    // @formatter:off
    public LoginResponse(
        Long id,
        String username,
        UserRole role,
        String token,
        VolunteerStatus volunteerStatus,
        String phone
    ) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
        this.volunteerStatus = volunteerStatus;
        this.phone = phone;
    }
    // @formatter:on

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

    public VolunteerStatus getVolunteerStatus() {
        return volunteerStatus;
    }

    public void setVolunteerStatus(VolunteerStatus volunteerStatus) {
        this.volunteerStatus = volunteerStatus;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
