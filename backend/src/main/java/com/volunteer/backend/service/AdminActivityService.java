package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.AdminActivityImportRequest;
import com.volunteer.backend.dto.AdminActivityUpdateRequest;
import com.volunteer.backend.dto.AdminSignupRecordResponse;
import com.volunteer.backend.dto.AdminSignupUpdateRequest;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class AdminActivityService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityRepository activityRepository;
    private final VolunteerRepository volunteerRepository;
    private final SignupRecordRepository signupRecordRepository;

    // @formatter:off
    public AdminActivityService(
        ActivityRepository activityRepository,
        VolunteerRepository volunteerRepository,
        SignupRecordRepository signupRecordRepository
    ) {
        this.activityRepository = activityRepository;
        this.volunteerRepository = volunteerRepository;
        this.signupRecordRepository = signupRecordRepository;
    }
    // @formatter:on

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

    public Activity updateActivity(Long activityId, AdminActivityUpdateRequest request) {
        Optional<Activity> a = activityRepository.findById(activityId);
        if (a.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }
        Activity activity = a.get();

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            activity.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            String desc = request.getDescription().trim();
            activity.setDescription(desc.isEmpty() ? null : desc);
        }

        if (request.getType() != null) {
            activity.setType(request.getType());
        }

        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            activity.setLocation(request.getLocation().trim());
        }

        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }

        if (request.getPointsPerHour() != null && request.getPointsPerHour() > 0) {
            activity.setPointsPerHour(request.getPointsPerHour());
        }

        if (request.getMaxParticipants() != null && request.getMaxParticipants() > 0) {
            activity.setMaxParticipants(request.getMaxParticipants());
        }

        if (request.getStatus() != null) {
            activity.setStatus(request.getStatus());
        }

        if (activity.getStartTime().isAfter(activity.getEndTime())) {
            throw new IllegalArgumentException("活动结束时间必须晚于开始时间");
        }

        return activityRepository.save(activity);
    }

    public void deleteActivity(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new IllegalArgumentException("活动不存在");
        }

        List<SignupRecord> signupRecords = signupRecordRepository.findByActivityIdOrderBySignupTimeDesc(activityId);
        for (SignupRecord record : signupRecords) {
            signupRecordRepository.delete(record);
        }

        activityRepository.deleteById(activityId);
    }

    public List<AdminSignupRecordResponse> getActivitySignups(Long activityId) {
        Optional<Activity> a = activityRepository.findById(activityId);
        if (a.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }

        List<SignupRecord> signupRecords = signupRecordRepository.findByActivityIdOrderBySignupTimeDesc(activityId);
        List<AdminSignupRecordResponse> responses = new ArrayList<>();

        for (SignupRecord record : signupRecords) {
            Optional<Volunteer> v = volunteerRepository.findById(record.getVolunteerId());
            if (v.isEmpty()) {
                continue;
            }
            Volunteer volunteer = v.get();

            String volunteerStartTimeStr = record.getVolunteerStartTime() != null
                    ? record.getVolunteerStartTime().format(DATETIME_FORMATTER)
                    : null;
            String volunteerEndTimeStr = record.getVolunteerEndTime() != null
                    ? record.getVolunteerEndTime().format(DATETIME_FORMATTER)
                    : null;
            String signupTimeStr = record.getSignupTime() != null
                    ? record.getSignupTime().format(DATETIME_FORMATTER)
                    : null;

            // @formatter:off
            AdminSignupRecordResponse response = new AdminSignupRecordResponse(
                record.getId(),
                record.getId(),
                volunteer.getId(),
                volunteer.getName(),
                volunteer.getPhone(),
                record.getStatus(),
                volunteerStartTimeStr,
                volunteerEndTimeStr,
                record.getActualHours(),
                record.getPoints(),
                signupTimeStr,
                record.getNote()
            );
            // @formatter:on
            responses.add(response);
        }

        return responses;
    }

    public SignupRecord updateSignupRecord(Long activityId, Long signupId, AdminSignupUpdateRequest request) {
        Optional<Activity> a = activityRepository.findById(activityId);
        if (a.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }

        Optional<SignupRecord> s = signupRecordRepository.findById(signupId);
        if (s.isEmpty()) {
            throw new IllegalArgumentException("报名记录不存在");
        }
        SignupRecord signupRecord = s.get();

        if (!signupRecord.getActivityId().equals(activityId)) {
            throw new IllegalArgumentException("报名记录不属于该活动");
        }

        if (request.getStatus() != null) {
            signupRecord.setStatus(request.getStatus());
        }

        if (request.getVolunteerStartTime() != null) {
            signupRecord.setVolunteerStartTime(request.getVolunteerStartTime());
        }

        if (request.getVolunteerEndTime() != null) {
            signupRecord.setVolunteerEndTime(request.getVolunteerEndTime());
        }

        if (request.getActualHours() != null) {
            signupRecord.setActualHours(request.getActualHours());
        }

        if (request.getPoints() != null) {
            signupRecord.setPoints(request.getPoints());
        }

        if (request.getNote() != null) {
            signupRecord.setNote(request.getNote().trim());
        }

        signupRecord.setUpdateTime(LocalDateTime.now());

        return signupRecordRepository.save(signupRecord);
    }
}
