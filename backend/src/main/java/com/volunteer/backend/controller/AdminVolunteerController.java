package com.volunteer.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.AdminVolunteerResponse;
import com.volunteer.backend.dto.AdminVolunteerReviewRequest;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerReviewAction;
import com.volunteer.backend.utils.VolunteerStatus;

@RestController
@RequestMapping("/api/admin/volunteers")
public class AdminVolunteerController {
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;

    public AdminVolunteerController(VolunteerRepository volunteerRepository, UserRepository userRepository) {
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AdminVolunteerResponse> getVolunteers(@RequestParam(defaultValue = "ALL") String status) {
        List<Volunteer> volunteers = switch (status.toUpperCase()) {
        case "ALL" -> volunteerRepository.findAll();
        case "PENDING" -> volunteerRepository.findByStatus(VolunteerStatus.PENDING);
        case "PROCESSED" -> volunteerRepository.findByStatusNot(VolunteerStatus.PENDING);
        default -> throw new IllegalArgumentException("未知的状态筛选");
        };

        return volunteers.stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/review")
    public AdminVolunteerResponse reviewVolunteer(@PathVariable Long id,
            @RequestBody AdminVolunteerReviewRequest request) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("志愿者申请不存在"));
        VolunteerReviewAction action = request.getAction();
        if (action == null) {
            throw new IllegalArgumentException("审核操作不能为空");
        }
        String note = request.getNote() != null ? request.getNote().trim() : "";
        if (note.isEmpty()) {
            throw new IllegalArgumentException("审核备注不能为空");
        }

        if (action == VolunteerReviewAction.APPROVE) {
            volunteer.approve(note);
            User user = userRepository.findById(volunteer.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.VOLUNTEER);
                userRepository.save(user);
            }
        } else {
            volunteer.reject(note);
        }

        Volunteer saved = volunteerRepository.save(volunteer);
        return toResponse(saved);
    }

    private AdminVolunteerResponse toResponse(Volunteer volunteer) {
        // @formatter:off
        return new AdminVolunteerResponse(
            volunteer.getId(),
            volunteer.getUserId(),
            volunteer.getName(),
            volunteer.getPhone(),
            volunteer.getStatus(),
            volunteer.getReviewNote(),
            volunteer.getCreateTime(),
            volunteer.getReviewTime()
        );
        // @formatter:on
    }
}