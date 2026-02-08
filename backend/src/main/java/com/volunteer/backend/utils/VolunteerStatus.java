package com.volunteer.backend.utils;

public enum VolunteerStatus {
    REVIEWING, // 审核中（默认）
    CERTIFIED, // 已认证
    REJECTED, // 已拒绝
    SUSPENDED // 已停用（违规等）
}
