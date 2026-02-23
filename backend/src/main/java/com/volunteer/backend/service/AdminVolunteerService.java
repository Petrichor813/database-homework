package com.volunteer.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.AdminVolunteerResponse;
import com.volunteer.backend.dto.AdminVolunteerReviewRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.UserRole;
import com.volunteer.backend.enums.VolunteerReviewAction;
import com.volunteer.backend.enums.VolunteerStatus;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class AdminVolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;

    public AdminVolunteerService(VolunteerRepository volunteerRepository, UserRepository userRepository) {
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<AdminVolunteerResponse> getVolunteers(String volunteerStatus, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页展示记录的数量不能小于等于0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Volunteer> volunteerPage;

        switch (volunteerStatus.toUpperCase()) {
        case "ALL":
            volunteerPage = volunteerRepository.findByDeletedFalse(pageable);
            break;
        case "REVIEWING":
            volunteerPage = volunteerRepository.findByStatusAndDeletedFalse(VolunteerStatus.REVIEWING, pageable);
            break;
        case "CERTIFIED":
            volunteerPage = volunteerRepository.findByStatusAndDeletedFalse(VolunteerStatus.CERTIFIED, pageable);
            break;
        case "SUSPENDED":
            volunteerPage = volunteerRepository.findByStatusAndDeletedFalse(VolunteerStatus.SUSPENDED, pageable);
            break;
        default:
            throw new IllegalArgumentException("未知的志愿者审核状态筛选");
        }

        List<Volunteer> volunteers = volunteerPage.getContent();
        List<AdminVolunteerResponse> responses = new ArrayList<>();

        for (Volunteer v : volunteers) {
            // @formatter:off
            AdminVolunteerResponse r = new AdminVolunteerResponse(
                v.getId(),
                v.getUserId(),
                v.getName(),
                v.getPhone(),
                v.getStatus(),
                v.getApplyReason(),
                v.getReviewNote(),
                v.getCreateTime(),
                v.getReviewTime()
            );
            // @formatter:on
            responses.add(r);
        }

        // @formatter:off
        return new PageResponse<>(
            responses,
            volunteerPage.getNumber(),
            volunteerPage.getSize(),
            volunteerPage.getTotalElements(),
            volunteerPage.getTotalPages()
        );
        // @formatter:on
    }

    public AdminVolunteerResponse reviewVolunteer(Long id, AdminVolunteerReviewRequest request) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(id);
        Volunteer volunteer;
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者申请不存在或该志愿者账号已注销");
        }
        volunteer = v.get();

        VolunteerReviewAction action = request.getAction();
        if (action == null) {
            throw new IllegalArgumentException("审核操作不能为空");
        }

        String note = (request.getNote() != null) ? request.getNote().trim() : "";
        if (note.isEmpty()) {
            throw new IllegalArgumentException("审核备注不能为空");
        }

        switch (action) {
        case APPROVE:
            volunteer.approve(note);

            Optional<User> u = userRepository.findByIdAndDeletedFalse(volunteer.getUserId());
            if (u.isEmpty()) {
                throw new IllegalArgumentException("用户不存在或该用户账号已注销");
            }
            User user = u.get();

            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.VOLUNTEER);
                userRepository.save(user);
            }
            break;
        case REJECT:
            volunteer.reject(note);
            break;
        case SUSPEND:
            volunteer.suspend(note);
            break;
        case RESUME:
            volunteer.resume(note);
            break;
        default:
            throw new IllegalArgumentException("未知的审核操作类型");
        }

        Volunteer saved = volunteerRepository.save(volunteer);
        // @formatter:off
        return new AdminVolunteerResponse(
            saved.getId(),
            saved.getUserId(),
            saved.getName(),
            saved.getPhone(),
            saved.getStatus(),
            saved.getApplyReason(),
            saved.getReviewNote(),
            saved.getCreateTime(),
            saved.getReviewTime()
        );
        // @formatter:on
    }
}