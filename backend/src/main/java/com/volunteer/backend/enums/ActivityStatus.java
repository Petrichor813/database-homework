package com.volunteer.backend.enums;

public enum ActivityStatus {
    RECRUITING, // 招募中（默认）
    CONFIRMED, // 已确认（人数已满，等待开始）
    ONGOING, // 进行中
    COMPLETED, // 已结束
    CANCELLED // 已取消
}
