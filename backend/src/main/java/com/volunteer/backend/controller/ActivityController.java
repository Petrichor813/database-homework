package com.volunteer.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.request.ActivityQueryRequest;
import com.volunteer.backend.dto.request.SignupRequest;
import com.volunteer.backend.dto.response.ActivityResponse;
import com.volunteer.backend.dto.response.PageResponse;
import com.volunteer.backend.dto.response.SignupResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.service.ActivityService;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // @formatter:off
    @GetMapping("/get-activities")
    public ResponseEntity<PageResponse<ActivityResponse>> getActivities(
        @AuthenticationPrincipal User user,
        @ModelAttribute ActivityQueryRequest request
    ) {
        // @formatter:on
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(activityService.getActivities(userId, request));
    }

    // @formatter:off
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signupActivity(
        @AuthenticationPrincipal User user,
        @RequestBody SignupRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(activityService.signupActivity(user.getId(), request));
    }

    // @formatter:off
    @PostMapping("/cancel-signup")
    public ResponseEntity<SignupResponse> cancelSignupActivity(
        @AuthenticationPrincipal User user,
        @RequestBody SignupRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(activityService.cancelSignupActivity(user.getId(), request));
    }

    // @formatter:off
    @GetMapping("/hot-activities")
    public ResponseEntity<List<ActivityResponse>> getHotActivities(
        @AuthenticationPrincipal User user
    ) {
        // @formatter:on
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(activityService.getHotActivities(userId));
    }
}