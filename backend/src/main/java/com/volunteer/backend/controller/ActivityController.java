package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ActivityResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.SignupRequest;
import com.volunteer.backend.dto.SignupResponse;
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
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") String type,
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "time") String sort
    ) {
        // @formatter:on
        Long userId = user != null ? user.getId() : null;
        return ResponseEntity.ok(activityService.getActivities(userId, page, size, keyword, type, status, date, sort));
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
}