package com.volunteer.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class VolunteerApplyRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String phone;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}