package com.volunteer.backend.controller;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.PointsRecordResponse;
import com.volunteer.backend.dto.UpdateUserProfileRequest;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.entity.PointsChangeRecord;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.PointsChangeRecordRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;

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

    private UserProfileResponse buildProfileResponse(User user) {
        Volunteer volunteer = volunteerRepository.findByUserId(user.getId()).orElse(null);
        Double points = 0.0;
        Integer serviceHours = 0;
        List<PointsRecordResponse> records = Collections.emptyList();

        if (volunteer != null) {
            Long volunteerId = volunteer.getId();
            points = pointsChangeRecordRepository.sumPointsByVolunteerId(volunteerId);
            serviceHours = signupRecordRepository.sumHoursByVolunteerId(volunteerId);
            // @formatter:off
            records = pointsChangeRecordRepository
                        .findTop5ByVolunteerIdOrderByChangeTimeDesc(volunteerId)
                        .stream()
                        .map(record -> toPointsRecordResponse(record))
                        .toList();
            // @formatter:on
        }

        // @formatter:off
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getPhone(),
            volunteer != null ? volunteer.getStatus() : null,
            points,
            serviceHours, 
            records
        );
        // @formatter:on
    }

    @GetMapping("/{userId}/profile")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return buildProfileResponse(user);
    }

    @PutMapping("/{userId}/profile")
    // @formatter:off
    public UserProfileResponse updateProfile(
        @PathVariable Long userId,
        @RequestBody UpdateUserProfileRequest request
    ) {
        // @formatter:on
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        user.setUsername(username);
        user.setPhone(request.getPhone());
        userRepository.save(user);

        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        if (volunteer != null) {
            volunteer.setPhone(user.getPhone());
            volunteerRepository.save(volunteer);
        }

        return buildProfileResponse(user);
    }

    @PostMapping("/{userId}/volunteer-apply")
    public UserProfileResponse applyVolunteer(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("管理员无需申请志愿者认证");
        }
        Volunteer existing = volunteerRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            throw new IllegalArgumentException("已提交过志愿者申请");
        }
        Volunteer volunteer = new Volunteer(user.getUsername(), user.getPhone(), userId);
        volunteerRepository.save(volunteer);
        return buildProfileResponse(user);
    }

    private PointsRecordResponse toPointsRecordResponse(PointsChangeRecord record) {
        String time = record.getChangeTime() != null ? record.getChangeTime().format(DATE_FORMATTER) : "";
        String type = record.getChangeType() != null ? record.getChangeType().name() : "";
        return new PointsRecordResponse(time, type, record.getChangePoints(), record.getReason());
    }
}