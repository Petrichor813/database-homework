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

        // @formatter:off
        Activity activity = new Activity(
            request.getTitle(),
            request.getType(),
            request.getLocation().trim(),
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
