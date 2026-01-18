package com.volunteer.backend.utils;

public enum SignupStatus {
    PENDING, // 待审核
    CONFIRMED, // 已确认
    PARTICIPATED, // 已参与
    CANCELLED, // 已取消
    REJECTED, // 已拒绝
    UNARRIVED // 未到场
}
