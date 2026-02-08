package com.volunteer.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.volunteer.backend.dto.PointsRecordResponse;
import com.volunteer.backend.dto.UpdateUserProfileRequest;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.dto.VolunteerApplyRequest;
import com.volunteer.backend.entity.PointsChangeRecord;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.PointsChangeRecordRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerStatus;

public class UserService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final PointsChangeRecordRepository pointsChangeRecordRepository;
    private final SignupRecordRepository signupRecordRepository;

    // @formatter:off
    public UserService(
        UserRepository userRepository,
        VolunteerRepository volunteerRepository,
        PointsChangeRecordRepository pointsChangeRecordRepository, SignupRecordRepository signupRecordRepository
    ) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
        this.pointsChangeRecordRepository = pointsChangeRecordRepository;
        this.signupRecordRepository = signupRecordRepository;
    }
    // @formatter:on

    private UserProfileResponse buildUserProfileResponse(User user) {
        Optional<Volunteer> v = volunteerRepository.findByUserId(user.getId());
        Volunteer volunteer = v.isPresent() ? v.get() : null;
        Double points = 0.0;
        Integer serviceHours = 0;
        List<PointsRecordResponse> responses = new ArrayList<>();

        if (volunteer != null) {
            Long volunteerId = volunteer.getId();
            points = pointsChangeRecordRepository.sumPointsByVolunteerId(volunteerId);
            serviceHours = signupRecordRepository.sumHoursByVolunteerId(volunteerId);
            List<PointsChangeRecord> records = pointsChangeRecordRepository
                    .findTop5ByVolunteerIdOrderByChangeTimeDesc(volunteerId);

            for (int i = 0; i < records.size(); i++) {
                PointsChangeRecord record = records.get(i);
                String time = (record.getChangeTime() != null) ? record.getChangeTime().format(DATE_FORMATTER) : "";
                String type = (record.getChangeType() != null) ? record.getChangeType().name() : "";
                // @formatter:off
                PointsRecordResponse r = new PointsRecordResponse(
                    time, type, record.getChangePoints(), record.getReason());
                // @formatter:on
                responses.add(r);
            }
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
            responses
        );
        // @formatter:on
    }

    public UserProfileResponse getProfile(Long userId) {
        Optional<User> u = userRepository.findById(userId);
        if (!u.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = u.get();
        return buildUserProfileResponse(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        Optional<User> u = userRepository.findById(userId);
        if (!u.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = u.get();

        String username = (request.getUsername() != null) ? request.getUsername().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        user.setUsername(username);
        user.setPhone(request.getPhone());
        userRepository.save(user);

        Optional<Volunteer> v = volunteerRepository.findByUserId(userId);
        Volunteer volunteer = v.isPresent() ? v.get() : null;
        if (volunteer != null) {
            volunteer.setPhone(user.getPhone());
            volunteerRepository.save(volunteer);
        }

        return buildUserProfileResponse(user);
    }

    public UserProfileResponse applyVolunteer(Long userId, VolunteerApplyRequest request) {
        Optional<User> u = userRepository.findById(userId);
        if (!u.isPresent()) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = u.get();

        String realName = (request.getRealName() != null) ? request.getRealName().trim() : "";
        if (realName.isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }

        String phone = (request.getPhone() != null) ? request.getPhone().trim() : "";
        if (phone.isEmpty()) {
            phone = user.getPhone();
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("管理员无需申请志愿者认证");
        }

        Optional<Volunteer> e = volunteerRepository.findByUserId(userId);
        Volunteer existedVolunteer = e.isPresent() ? e.get() : null;
        if (existedVolunteer != null) {
            if (existedVolunteer.getStatus() == VolunteerStatus.REJECTED) {
                existedVolunteer.resetForReapply(realName, phone);
                volunteerRepository.save(existedVolunteer);
                return buildUserProfileResponse(user);
            }
            throw new IllegalArgumentException("已提交过志愿者申请");
        }

        Volunteer volunteer = new Volunteer(realName, phone, userId);
        volunteerRepository.save(volunteer);
        return buildUserProfileResponse(user);
    }
}
