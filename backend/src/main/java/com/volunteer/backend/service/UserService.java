package com.volunteer.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.ModifyVolunteerApplicationRequest;
import com.volunteer.backend.dto.UpdateUserProfileRequest;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.dto.VolunteerApplyRequest;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.PointChangeRecordRepository;
import com.volunteer.backend.repository.SignupRecordRepository;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerStatus;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final SignupRecordRepository signupRecordRepository;
    private final PointChangeRecordRepository pointChangeRecordRepository;

    // @formatter:off
    public UserService(
        UserRepository userRepository,
        VolunteerRepository volunteerRepository,
        SignupRecordRepository signupRecordRepository,
        PointChangeRecordRepository pointChangeRecordRepository
    ) {
        this.userRepository = userRepository;
        this.volunteerRepository = volunteerRepository;
        this.signupRecordRepository = signupRecordRepository;
        this.pointChangeRecordRepository = pointChangeRecordRepository;
    }
    // @formatter:on

    private User findActiveUser(Long userId) {
        Optional<User> u = userRepository.findByIdAndDeletedFalse(userId);
        if (!u.isPresent()) {
            throw new IllegalArgumentException("用户不存在或该用户账号已注销");
        }
        return u.get();
    }

    private UserProfileResponse buildUserProfileResponse(User user) {
        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(user.getId());
        Volunteer volunteer = v.isPresent() ? v.get() : null;
        Double points = 0.0;
        Integer serviceHours = 0;

        if (volunteer != null) {
            Long volunteerId = volunteer.getId();
            serviceHours = signupRecordRepository.sumHoursByVolunteerId(volunteerId);
            points = pointChangeRecordRepository.sumPointsByVolunteerId(volunteerId);
        }

        // @formatter:off
        return new UserProfileResponse(
            user.getId(),
            volunteer != null ? volunteer.getId() : null,
            user.getUsername(),
            user.getRole(),
            user.getPhone(),
            volunteer != null ? volunteer.getName() : null,
            volunteer != null ? volunteer.getStatus() : null,
            volunteer != null ? volunteer.getApplyReason() : null,
            points,
            serviceHours
        );
        // @formatter:on
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = findActiveUser(userId);
        return buildUserProfileResponse(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = findActiveUser(userId);

        String username = (request.getUsername() != null) ? request.getUsername().trim() : "";
        if (username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!username.equals(user.getUsername()) && userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        user.setUsername(username);
        user.setPhone(request.getPhone());
        userRepository.save(user);

        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        Volunteer volunteer = v.isPresent() ? v.get() : null;
        if (volunteer != null) {
            volunteer.setPhone(user.getPhone());
            volunteerRepository.save(volunteer);
        }

        return buildUserProfileResponse(user);
    }

    public UserProfileResponse applyVolunteer(Long userId, VolunteerApplyRequest request) {
        User user = findActiveUser(userId);
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

        Optional<Volunteer> e = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        Volunteer existedVolunteer = e.isPresent() ? e.get() : null;
        if (existedVolunteer != null) {
            if (existedVolunteer.getStatus() == VolunteerStatus.REJECTED) {
                existedVolunteer.resetForReapply(realName, phone);
                existedVolunteer.setApplyReason(request.getApplyReason());
                volunteerRepository.save(existedVolunteer);
                return buildUserProfileResponse(user);
            }
            throw new IllegalArgumentException("已提交过志愿者申请");
        }

        Volunteer volunteer = new Volunteer(realName, phone, userId);
        volunteer.setApplyReason(request.getApplyReason());
        volunteerRepository.save(volunteer);
        return buildUserProfileResponse(user);
    }

    public UserProfileResponse updateVolunteerApplication(Long userId, ModifyVolunteerApplicationRequest request) {
        User user = findActiveUser(userId);

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("管理员无需修改志愿者申请");
        }

        Optional<Volunteer> e = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        Volunteer volunteer = e.isPresent() ? e.get() : null;

        if (volunteer == null) {
            throw new IllegalArgumentException("未找到志愿者申请记录");
        }

        if (volunteer.getStatus() != VolunteerStatus.REVIEWING) {
            throw new IllegalArgumentException("只能修改审核中的申请");
        }

        String realName = (request.getRealName() != null) ? request.getRealName().trim() : "";
        if (realName.isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }

        String phone = (request.getPhone() != null) ? request.getPhone().trim() : "";
        if (phone.isEmpty()) {
            phone = user.getPhone();
        }

        volunteer.setName(realName);
        volunteer.setPhone(phone);
        volunteer.setApplyReason(request.getApplyReason());
        volunteerRepository.save(volunteer);

        return buildUserProfileResponse(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findActiveUser(userId);
        user.markDeleted();
        userRepository.save(user);

        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        if (!v.isPresent()) {
            return;
        }

        Volunteer volunteer = v.get();
        volunteer.markDeleted();
        volunteerRepository.save(volunteer);
    }
}