package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.ActivityResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.SignupRequest;
import com.volunteer.backend.dto.SignupResponse;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.ActivityType;
import com.volunteer.backend.enums.SignupStatus;
import com.volunteer.backend.enums.VolunteerStatus;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class ActivityService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityRepository activityRepository;
    private final VolunteerRepository volunteerRepository;
    private final SignupRecordRepository signupRecordRepository;

    // @formatter:off
    public ActivityService(
        ActivityRepository activityRepository,
        VolunteerRepository volunteerRepository,
        SignupRecordRepository signupRecordRepository
    ) {
        this.activityRepository = activityRepository;
        this.volunteerRepository = volunteerRepository;
        this.signupRecordRepository = signupRecordRepository;
    }
    // @formatter:on

    private void refreshActivityStatus(Activity activity, LocalDateTime now) {
        if (activity.getStatus() == ActivityStatus.CANCELLED) {
            return;
        }

        if (now.isAfter(activity.getEndTime())) {
            activity.setStatus(ActivityStatus.COMPLETED);
            return;
        }

        if (now.isAfter(activity.getStartTime()) || now.isEqual(activity.getStartTime())) {
            activity.setStatus(ActivityStatus.ONGOING);
        } else if (activity.getStatus() == ActivityStatus.COMPLETED) {
            activity.setStatus(ActivityStatus.RECRUITING);
        }
    }

    private ActivityResponse buildResponse(Activity activity, SignupStatus signupStatus) {
        // @formatter:off
        return new ActivityResponse(
            activity.getId(),
            activity.getTitle(),
            activity.getDescription(),
            activity.getType(),
            activity.getLocation(),
            activity.getStartTime().format(DATETIME_FORMATTER),
            activity.getEndTime().format(DATETIME_FORMATTER),
            activity.getStatus(),
            activity.getPointsPerHour(),
            activity.getMaxParticipants(),
            activity.getCurParticipants(),
            signupStatus
        );
        // @formatter:on
    }

    // @formatter:off
    public PageResponse<ActivityResponse> getActivities(
        Long userId,
        int page,
        int size,
        String keyword,
        String type,
        String status,
        String date,
        String sort
    ) {
        // @formatter:on
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        Pageable pageable;
        if ("status".equalsIgnoreCase(sort)) {
            pageable = PageRequest.of(page, size);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        }

        ActivityType activityType = null;
        if (type != null && !type.isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            try {
                activityType = ActivityType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的活动类型: " + type);
            }
        }

        ActivityStatus activityStatus = null;
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            try {
                activityStatus = ActivityStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的活动状态: " + status);
            }
        }

        Page<Activity> activityPage;
        if ("status".equalsIgnoreCase(sort)) {
            activityPage = activityRepository.findActivitiesByStatusOrder(
                keyword != null ? keyword : "",
                activityType,
                activityStatus,
                date != null ? date : "",
                pageable
            );
        } else {
            activityPage = activityRepository.findActivities(
                keyword != null ? keyword : "",
                activityType,
                activityStatus,
                date != null ? date : "",
                pageable
            );
        }

        LocalDateTime now = LocalDateTime.now();
        List<ActivityResponse> content = new ArrayList<>();

        Map<Long, SignupStatus> signupStatusMap = new HashMap<>();
        if (userId != null) {
            Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
            if (v.isPresent()) {
                Volunteer volunteer = v.get();
                if (volunteer.getStatus() == VolunteerStatus.CERTIFIED) {
                    List<Long> activityIds = new ArrayList<>();
                    for (Activity activity : activityPage.getContent()) {
                        activityIds.add(activity.getId());
                    }
                    if (!activityIds.isEmpty()) {
                        List<SignupRecord> signupRecords = signupRecordRepository.findByVolunteerIdAndActivityIds(
                            volunteer.getId(), activityIds);
                        for (SignupRecord record : signupRecords) {
                            signupStatusMap.put(record.getActivityId(), record.getStatus());
                        }
                    }
                }
            }
        }

        for (Activity activity : activityPage.getContent()) {
            refreshActivityStatus(activity, now);
            SignupStatus signupStatus = signupStatusMap.get(activity.getId());
            content.add(buildResponse(activity, signupStatus));
        }

        return new PageResponse<>(
            content,
            page,
            size,
            (int) activityPage.getTotalElements(),
            activityPage.getTotalPages()
        );
    }

    @Transactional
    public SignupResponse signupActivity(Long userId, SignupRequest request) {
        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("您还未申请成为志愿者或志愿者身份未认证");
        }
        Volunteer volunteer = v.get();

        if (volunteer.getStatus() != VolunteerStatus.CERTIFIED) {
            throw new IllegalArgumentException("您的志愿者身份未认证，无法报名活动");
        }

        Optional<Activity> a = activityRepository.findById(request.getActivityId());
        if (a.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }
        Activity activity = a.get();

        if (activity.getStatus() != ActivityStatus.RECRUITING && activity.getStatus() != ActivityStatus.CONFIRMED) {
            throw new IllegalArgumentException("该活动当前不接受报名");
        }

        boolean hasSignedUp = signupRecordRepository.existsByVolunteerIdAndActivityId(volunteer.getId(),
                request.getActivityId());
        if (hasSignedUp) {
            throw new IllegalArgumentException("您已经报名了该活动");
        }

        if (activity.getMaxParticipants() != null && activity.getCurParticipants() >= activity.getMaxParticipants()) {
            throw new IllegalArgumentException("活动报名人数已满");
        }

        LocalDateTime volunteerStartTime = null;
        LocalDateTime volunteerEndTime = null;

        if (request.getVolunteerStartTime() != null && !request.getVolunteerStartTime().isEmpty()) {
            try {
                volunteerStartTime = LocalDateTime.parse(request.getVolunteerStartTime(), DATETIME_FORMATTER);
            } catch (Exception e) {
                throw new IllegalArgumentException("开始时间格式不正确，请使用 yyyy-MM-dd HH:mm 格式");
            }
        }

        if (request.getVolunteerEndTime() != null && !request.getVolunteerEndTime().isEmpty()) {
            try {
                volunteerEndTime = LocalDateTime.parse(request.getVolunteerEndTime(), DATETIME_FORMATTER);
            } catch (Exception e) {
                throw new IllegalArgumentException("结束时间格式不正确，请使用 yyyy-MM-dd HH:mm 格式");
            }
        }

        // @formatter:off
        SignupRecord signupRecord = new SignupRecord(
            volunteer.getId(),
            request.getActivityId(),
            volunteerStartTime,
            volunteerEndTime
        );
        // @formatter:on

        signupRecord.setStatus(SignupStatus.REVIEWING);
        signupRecord.setUpdateTime(LocalDateTime.now());

        signupRecord = signupRecordRepository.save(signupRecord);

        activity.setCurParticipants(activity.getCurParticipants() + 1);
        activityRepository.save(activity);

        return new SignupResponse(signupRecord.getId(), "报名成功");
    }

    @Transactional
    public SignupResponse cancelSignupActivity(Long userId, SignupRequest request) {
        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("您还未申请成为志愿者或志愿者身份未认证");
        }
        Volunteer volunteer = v.get();

        Optional<Activity> a = activityRepository.findById(request.getActivityId());
        if (a.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }
        Activity activity = a.get();

        Optional<SignupRecord> s = signupRecordRepository.findByVolunteerIdAndActivityId(volunteer.getId(),
                request.getActivityId());
        if (s.isEmpty()) {
            throw new IllegalArgumentException("您还未报名该活动");
        }
        SignupRecord signupRecord = s.get();

        if (signupRecord.getStatus() == SignupStatus.CANCELLED) {
            throw new IllegalArgumentException("您已经取消了该活动的报名");
        }

        if (activity.getStatus() == ActivityStatus.ONGOING || activity.getStatus() == ActivityStatus.COMPLETED) {
            throw new IllegalArgumentException("活动已经开始或已结束，无法取消报名");
        }

        signupRecord.setStatus(SignupStatus.CANCELLED);
        signupRecord.setUpdateTime(LocalDateTime.now());
        signupRecordRepository.save(signupRecord);

        activity.setCurParticipants(Math.max(0, activity.getCurParticipants() - 1));
        activityRepository.save(activity);

        return new SignupResponse(signupRecord.getId(), "取消报名成功");
    }
}