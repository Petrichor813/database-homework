package com.volunteer.backend.service;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.AdminActivityImportRequest;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.enums.ActivityStatus;

@Service
public class AdminActivityService {
    private final ActivityRepository activityRepository;

    public AdminActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity importActivity(AdminActivityImportRequest request) throws IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("活动导入请求不能为空");
        }

        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("活动名称不能为空");
        }

        String location = request.getLocation();
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("活动地点不能为空");
        }

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("活动开始时间不能为空");
        }

        if (request.getEndTime() == null) {
            throw new IllegalArgumentException("活动结束时间不能为空");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("活动结束时间必须晚于开始时间");
        }

        if (request.getPointsPerHour() == null || request.getPointsPerHour() <= 0) {
            throw new IllegalArgumentException("每小时积分必须大于0");
        }

        if (request.getMaxParticipants() == null || request.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("活动人数上限必须大于0");
        }

        if (request.getType() == null) {
            throw new IllegalArgumentException("活动类型不能为空");
        }

        // @formatter:off
        Activity activity = new Activity(
            title.trim(),
            request.getType(),
            location.trim(),
            request.getStartTime(),
            request.getEndTime(),
            request.getPointsPerHour(),
            request.getMaxParticipants()
        );
        // @formatter:on

        activity.setStatus(request.getStatus() == null ? ActivityStatus.RECRUITING : request.getStatus());

        String desc = request.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = null;
        } else {
            desc = desc.trim();
        }
        activity.setDescription(desc);

        return activityRepository.save(activity);
    }
}
