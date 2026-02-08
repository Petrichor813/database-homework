package com.volunteer.backend.utils;

public enum ExchangeStatus {
    REVIEWING, // 待处理
    PROCESSING, // 处理中（管理员已接单）
    COMPLETED, // 已完成（已发放/已领取）
    CANCELLED, // 已取消（用户取消）
    REJECTED // 已拒绝（管理员拒绝）
}
