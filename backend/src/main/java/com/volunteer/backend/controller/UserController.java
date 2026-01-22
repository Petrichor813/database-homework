package com.volunteer.backend.controller;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.PointsRecordResponse;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.entity.PointsChangeRecord;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.PointsChangeRecordRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final PointsChangeRecordRepository pointsChangeRecordRepository;
    private final SignupRecordRepository signupRecordRepository;

    // @formatter:off
    public UserController(
        UserRepository userRepository,
        VolunteerRepository volunteerRepository,
        PointsChangeRecordRepository pointsChangeRecordRepository, SignupRecordRepository signupRecordRepository
    ) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
        this.pointsChangeRecordRepository = pointsChangeRecordRepository;
        this.signupRecordRepository = signupRecordRepository;
    }
    // formatter:on

    @GetMapping("/{userId}/profile")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        Double points = 0.0;
        Integer serviceHours = 0;
        List<PointsRecordResponse> records = Collections.emptyList();

        if (volunteer != null) {
            Long volunteerId = volunteer.getId();
            points = pointsChangeRecordRepository.sumPointsByVolunteerId(volunteerId);
            serviceHours = signupRecordRepository.sumHoursByVolunteerId(volunteerId);
            records = pointsChangeRecordRepository.findTop5ByVolunteerIdOrderByChangeTimeDesc(volunteerId).stream()
                    .map(record -> toPointsRecordResponse(record)).toList();
        }

        return new UserProfileResponse(user.getId(), user.getUsername(), user.getRole(), user.getPhone(), volunteer != null ? volunteer.getStatus() : null, points, serviceHours, records);
    }

    private PointsRecordResponse toPointsRecordResponse(PointsChangeRecord record) {
        String time = record.getChangeTime() != null ? record.getChangeTime().format(DATE_FORMATTER) : "";
        String type = record.getChangeType() != null ? record.getChangeType().name() : "";
        return new PointsRecordResponse(time, type, record.getChangePoints(), record.getReason());
    }
}