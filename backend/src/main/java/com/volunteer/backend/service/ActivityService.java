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

import com.volunteer.backend.dto.request.ActivityQueryRequest;
import com.volunteer.backend.dto.request.SignupRequest;
import com.volunteer.backend.dto.response.ActivityResponse;
import com.volunteer.backend.dto.response.PageResponse;
import com.volunteer.backend.dto.response.SignupResponse;
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
        // @formatter:on
        this.activityRepository = activityRepository;
        this.volunteerRepository = volunteerRepository;
        this.signupRecordRepository = signupRecordRepository;
    }

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
        ActivityQueryRequest request
    ) {
        // @formatter:on
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (request.getSize() <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        Pageable pageable;
        if ("status".equalsIgnoreCase(request.getSort())) {
            pageable = PageRequest.of(request.getPage(), request.getSize());
        } else {
            pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "startTime"));
        }

        ActivityType activityType = null;
        if (request.getType() != null && !request.getType().isEmpty() && !"ALL".equalsIgnoreCase(request.getType())) {
            try {
                activityType = ActivityType.valueOf(request.getType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的活动类型: " + request.getType());
            }
        }

        ActivityStatus activityStatus = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()
                && !"ALL".equalsIgnoreCase(request.getStatus())) {
            try {
                activityStatus = ActivityStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的活动状态: " + request.getStatus());
            }
        }

        Page<Activity> activityPage;
        if ("status".equalsIgnoreCase(request.getSort())) {
            activityPage = activityRepository.findActivitiesByStatusOrder(
                    request.getKeyword() != null ? request.getKeyword() : "", activityType, activityStatus,
                    request.getDate() != null ? request.getDate() : "", pageable);
        } else {
            activityPage = activityRepository.findActivities(request.getKeyword() != null ? request.getKeyword() : "",
                    activityType, activityStatus, request.getDate() != null ? request.getDate() : "", pageable);
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
                        List<SignupRecord> signupRecords = signupRecordRepository
                                .findByVolunteerIdAndActivityIds(volunteer.getId(), activityIds);
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

        // @formatter:off
        return new PageResponse<>(
            content,
            request.getPage(),
            request.getSize(),
            (int) activityPage.getTotalElements(),
            activityPage.getTotalPages()
        );
        // @formatter:on
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

        boolean hasSignedUp = signupRecordRepository.existsByVolunteerIdAndActivityIdAndStatusNot(volunteer.getId(),
                request.getActivityId(), SignupStatus.CANCELLED);
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
                throw new IllegalArgumentException("开始时间格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }

        if (request.getVolunteerEndTime() != null && !request.getVolunteerEndTime().isEmpty()) {
            try {
                volunteerEndTime = LocalDateTime.parse(request.getVolunteerEndTime(), DATETIME_FORMATTER);
            } catch (Exception e) {
                throw new IllegalArgumentException("结束时间格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式");
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

    public List<ActivityResponse> getHotActivities(Long userId) {
        Pageable pageable = PageRequest.of(0, 3);
        List<Activity> activities = activityRepository.findHotActivities(pageable);

        LocalDateTime now = LocalDateTime.now();
        List<ActivityResponse> content = new ArrayList<>();

        Map<Long, SignupStatus> signupStatusMap = new HashMap<>();
        if (userId != null) {
            Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
            if (v.isPresent()) {
                Volunteer volunteer = v.get();
                if (volunteer.getStatus() == VolunteerStatus.CERTIFIED) {
                    List<Long> activityIds = new ArrayList<>();
                    for (Activity activity : activities) {
                        activityIds.add(activity.getId());
                    }
                    if (!activityIds.isEmpty()) {
                        List<SignupRecord> signupRecords = signupRecordRepository
                                .findByVolunteerIdAndActivityIds(volunteer.getId(), activityIds);
                        for (SignupRecord record : signupRecords) {
                            signupStatusMap.put(record.getActivityId(), record.getStatus());
                        }
                    }
                }
            }
        }

        for (Activity activity : activities) {
            refreshActivityStatus(activity, now);
            SignupStatus signupStatus = signupStatusMap.get(activity.getId());
            content.add(buildResponse(activity, signupStatus));
        }

        return content;
    }
}