package com.volunteer.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.AdminVolunteerResponse;
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

    private AdminVolunteerResponse buildResponse(Volunteer volunteer) {
        // @formatter:off
        return new AdminVolunteerResponse(
            volunteer.getId(),
            volunteer.getUserId(),
            volunteer.getName(),
            volunteer.getPhone(),
            volunteer.getStatus(),
            volunteer.getApplyReason(),
            volunteer.getReviewNote(),
            volunteer.getCreateTime(),
            volunteer.getReviewTime()
        );
        // @formatter:on
    }

    public PageResponse<AdminVolunteerResponse> getVolunteers(String volunteerStatus, int page, int size, String keyword) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页展示记录的数量不能小于等于0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Volunteer> volunteerPage;

        String trimmedKeyword = (keyword != null) ? keyword.trim() : "";

        if (!trimmedKeyword.isEmpty()) {
            switch (volunteerStatus.toUpperCase()) {
            case "ALL":
                volunteerPage = volunteerRepository.findByKeywordAndDeletedFalse(trimmedKeyword, pageable);
                break;
            case "REVIEWING":
                volunteerPage = volunteerRepository.findByKeywordAndStatusAndDeletedFalse(trimmedKeyword, VolunteerStatus.REVIEWING, pageable);
                break;
            case "CERTIFIED":
                volunteerPage = volunteerRepository.findByKeywordAndStatusAndDeletedFalse(trimmedKeyword, VolunteerStatus.CERTIFIED, pageable);
                break;
            case "SUSPENDED":
                volunteerPage = volunteerRepository.findByKeywordAndStatusAndDeletedFalse(trimmedKeyword, VolunteerStatus.SUSPENDED, pageable);
                break;
            default:
                throw new IllegalArgumentException("未知的志愿者审核状态筛选");
            }
        } else {
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
        }

        List<Volunteer> volunteers = volunteerPage.getContent();
        List<AdminVolunteerResponse> responses = new ArrayList<>();

        for (Volunteer v : volunteers) {
            responses.add(buildResponse(v));
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

    public AdminVolunteerResponse reviewVolunteer(Long id, String action, String note) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(id);
        Volunteer volunteer;
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者申请不存在或该志愿者账号已注销");
        }
        volunteer = v.get();

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("审核操作不能为空");
        }

        VolunteerReviewAction reviewAction;
        try {
            reviewAction = VolunteerReviewAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的审核操作类型: " + action);
        }

        String trimmedNote = (note != null) ? note.trim() : "";
        if (trimmedNote.isEmpty()) {
            throw new IllegalArgumentException("审核备注不能为空");
        }

        switch (reviewAction) {
        case APPROVE:
            volunteer.approve(trimmedNote);

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
            volunteer.reject(trimmedNote);
            break;
        case SUSPEND:
            volunteer.suspend(trimmedNote);
            break;
        case RESUME:
            volunteer.resume(trimmedNote);
            break;
        default:
            throw new IllegalArgumentException("未知的审核操作类型");
        }

        Volunteer saved = volunteerRepository.save(volunteer);
        return buildResponse(saved);
    }

    public PageResponse<AdminVolunteerResponse> searchVolunteers(String keyword, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页展示记录的数量不能小于等于0");
        }

        String trimmedKeyword = (keyword != null) ? keyword.trim() : "";
        if (trimmedKeyword.isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Volunteer> volunteerPage = volunteerRepository.findByKeywordAndDeletedFalse(trimmedKeyword, pageable);

        List<Volunteer> volunteers = volunteerPage.getContent();
        List<AdminVolunteerResponse> responses = new ArrayList<>();

        for (Volunteer v : volunteers) {
            responses.add(buildResponse(v));
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
}