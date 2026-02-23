package com.volunteer.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.ActivityResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.SignupRequest;
import com.volunteer.backend.dto.SignupResponse;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ActivityStatus;
import com.volunteer.backend.enums.SignupStatus;
import com.volunteer.backend.enums.VolunteerStatus;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class ActivityService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    private int statusOrder(ActivityStatus status) {
        int weight;
        switch (status) {
        case RECRUITING, CONFIRMED:
            weight = 0;
            break;
        case ONGOING:
            weight = 1;
            break;
        case COMPLETED, CANCELLED:
            weight = 2;
            break;
        default:
            weight = 3;
        }
        return weight;
    }

    private Comparator<Activity> buildComparator(String sort) {
        // 使用匿名内部类代替lambda表达式
        if ("status".equalsIgnoreCase(sort)) {
            return new Comparator<Activity>() {
                @Override
                public int compare(Activity a1, Activity a2) {
                    // 先按状态排序
                    int statusCompare = Integer.compare(statusOrder(a1.getStatus()), statusOrder(a2.getStatus()));
                    if (statusCompare != 0) {
                        return statusCompare;
                    }
                    // 状态相同则按开始时间倒序
                    return a2.getStartTime().compareTo(a1.getStartTime());
                }
            };
        }

        // 默认按开始时间倒序排序
        return new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                return a2.getStartTime().compareTo(a1.getStartTime());
            }
        };
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

    private ActivityResponse mapToResponse(Activity activity, Long userId) {
        // 获取用户的报名状态
        SignupStatus signupStatus = null;

        if (userId != null) {
            Optional<Volunteer> v = volunteerRepository.findByUserId(userId);
            if (v.isPresent()) {
                Volunteer volunteer = v.get();
                if (volunteer.getStatus() == VolunteerStatus.CERTIFIED) {
                    Optional<SignupRecord> sr = signupRecordRepository.findByVolunteerIdAndActivityId(volunteer.getId(),
                            activity.getId());
                    if (sr.isPresent()) {
                        signupStatus = sr.get().getStatus();
                    }
                }
            }
        }

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

        List<Activity> allActivities = new ArrayList<>(activityRepository.findAll());
        LocalDateTime now = LocalDateTime.now();

        // 使用一般循环代替流式写法
        List<Activity> filtered = new ArrayList<>();
        for (Activity activity : allActivities) {
            refreshActivityStatus(activity, now);
            boolean shouldInclude = true;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
                String title = activity.getTitle() == null ? "" : activity.getTitle().toLowerCase(Locale.ROOT);
                String description = activity.getDescription() == null ? ""
                        : activity.getDescription().toLowerCase(Locale.ROOT);
                if (!title.contains(lowerKeyword) && !description.contains(lowerKeyword)) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude && type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                if (!activity.getType().name().equalsIgnoreCase(type)) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude && status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                if (!activity.getStatus().name().equalsIgnoreCase(status)) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude && date != null && !date.isBlank()) {
                LocalDate filterDate = LocalDate.parse(date);
                LocalDate activityDate = activity.getStartTime().toLocalDate();
                if (!filterDate.equals(activityDate)) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude) {
                filtered.add(activity);
            }
        }

        // 使用一般循环代替流式排序和映射
        Comparator<Activity> comparator = buildComparator(sort);
        filtered.sort(comparator);

        List<ActivityResponse> content = new ArrayList<>();
        for (Activity activity : filtered) {
            content.add(mapToResponse(activity, userId));
        }

        int totalElements = content.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        if (start >= totalElements) {
            return new PageResponse<>(new ArrayList<>(), page, size, totalElements, totalPages);
        }
        int end = Math.min(start + size, totalElements);
        List<ActivityResponse> pagedContent = content.subList(start, end);

        return new PageResponse<>(pagedContent, page, size, totalElements, totalPages);
    }

    public SignupResponse signupActivity(Long userId, SignupRequest request) {
        Optional<Volunteer> v = volunteerRepository.findById(userId);
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

    public SignupResponse cancelSignupActivity(Long userId, SignupRequest request) {
        Optional<Volunteer> v = volunteerRepository.findById(userId);
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