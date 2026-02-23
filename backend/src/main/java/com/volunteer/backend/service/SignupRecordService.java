package com.volunteer.backend.service;

import java.util.Optional;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.SignupRecordResponse;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.ActivityRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class SignupRecordService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VolunteerRepository volunteerRepository;
    private final ActivityRepository activityRepository;
    private final SignupRecordRepository signupRecordRepository;

    // @formatter:off
    public SignupRecordService(
        VolunteerRepository volunteerRepository,
        ActivityRepository activityRepository,
        SignupRecordRepository signupRecordRepository
    ) {
        this.volunteerRepository = volunteerRepository;
        this.activityRepository = activityRepository;
        this.signupRecordRepository = signupRecordRepository;
    }
    // @formatter:on

    public PageResponse<SignupRecordResponse> getSignupRecords(Long volunteerId, int page, int size) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("未找到志愿者信息");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<SignupRecord> recordPage = signupRecordRepository.findByVolunteerIdOrderBySignupTimeDesc(volunteerId,
                pageable);

        List<SignupRecord> records = recordPage.getContent();
        List<SignupRecordResponse> content = new ArrayList<>();

        for (SignupRecord r : records) {
            Optional<Activity> a = activityRepository.findById(r.getActivityId());
            if (a.isEmpty()) {
                throw new IllegalArgumentException("活动不存在");
            }
            Activity activity = a.get();
            // @formatter:off
            content.add(
                new SignupRecordResponse(
                    r.getId(),
                    activity.getTitle(),
                    activity.getStartTime().format(DATETIME_FORMATTER),
                    activity.getEndTime().format(DATETIME_FORMATTER),
                    r.getVolunteerStartTime() != null ? r.getVolunteerStartTime().format(DATETIME_FORMATTER) : null,
                    r.getVolunteerEndTime() != null ? r.getVolunteerEndTime().format(DATETIME_FORMATTER) : null,
                    r.getStatus().toString(),
                    r.getSignupTime().format(DATETIME_FORMATTER),
                    r.getActualHours(),
                    r.getPoints(),
                    r.getNote()
                )
            );
            // @formatter:on
        }

        // @formatter:off
        return new PageResponse<>(
            content,
            page,
            size,
            recordPage.getTotalElements(),
            recordPage.getTotalPages()
        );
        // @formatter:on
    }
}
