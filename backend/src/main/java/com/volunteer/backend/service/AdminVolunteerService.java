package com.volunteer.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.volunteer.backend.dto.AdminVolunteerResponse;
import com.volunteer.backend.dto.AdminVolunteerReviewRequest;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.UserRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.utils.UserRole;
import com.volunteer.backend.utils.VolunteerReviewAction;
import com.volunteer.backend.utils.VolunteerStatus;
import org.springframework.stereotype.Service;

@Service
public class AdminVolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final UserRepository userRepository;

    public AdminVolunteerService(VolunteerRepository volunteerRepository, UserRepository userRepository) {
        this.volunteerRepository = volunteerRepository;
        this.userRepository = userRepository;
    }

    public List<AdminVolunteerResponse> getVolunteers(String volunteerStatus) {
        List<Volunteer> volunteers;
        List<AdminVolunteerResponse> responses = new ArrayList<>();

        switch (volunteerStatus.toUpperCase()) {
        case "ALL":
            volunteers = volunteerRepository.findAll();
            break;
        case "REVIEWING":
            volunteers = volunteerRepository.findByStatus(VolunteerStatus.REVIEWING);
            break;
        case "PROCESSED":
            volunteers = volunteerRepository.findByStatusNot(VolunteerStatus.REVIEWING);
            break;
        default:
            throw new IllegalArgumentException("未知的志愿者审核状态筛选");
        }

        for (int i = 0; i < volunteers.size(); i++) {
            Volunteer v = volunteers.get(i);
            // @formatter:off
            AdminVolunteerResponse r = new AdminVolunteerResponse(
                v.getId(),
                v.getUserId(),
                v.getName(),
                v.getPhone(),
                v.getStatus(),
                v.getReviewNote(),
                v.getCreateTime(),
                v.getReviewTime()
            );
            // @formatter:on
            responses.add(r);
        }

        return responses;
    }

    public AdminVolunteerResponse reviewVolunteer(Long id, AdminVolunteerReviewRequest request) {
        Optional<Volunteer> v = volunteerRepository.findById(id);
        Volunteer volunteer;
        if (!v.isPresent()) {
            throw new IllegalArgumentException("志愿者申请不存在");
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

        if (action == VolunteerReviewAction.APPROVE) {
            volunteer.approve(note);

            Optional<User> u = userRepository.findById(volunteer.getUserId());
            if (!u.isPresent()) {
                throw new IllegalArgumentException("用户不存在");
            }
            User user = u.get();

            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.VOLUNTEER);
                userRepository.save(user);
            }
        } else {
            volunteer.reject(note);
        }

        Volunteer saved = volunteerRepository.save(volunteer);
        // @formatter:off
        return new AdminVolunteerResponse(
            saved.getId(),
            saved.getUserId(),
            saved.getName(),
            saved.getPhone(),
            saved.getStatus(),
            saved.getReviewNote(),
            saved.getCreateTime(),
            saved.getReviewTime()
        );
        // @formatter:on
    }
}
