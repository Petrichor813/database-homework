package com.volunteer.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.UpdateUserProfileRequest;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.dto.VolunteerApplyRequest;
import com.volunteer.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/profile")
    public UserProfileResponse getProfile(@PathVariable Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/{userId}/profile")
    // @formatter:off
    public UserProfileResponse updateProfile(
        @PathVariable Long userId,
        @RequestBody UpdateUserProfileRequest request
    ) {
        // @formatter:on
        return userService.updateProfile(userId, request);
    }

    @PostMapping("/{userId}/volunteer-apply")
    // @formatter:off
    public UserProfileResponse applyVolunteer(
        @PathVariable Long userId,
        @Valid @RequestBody VolunteerApplyRequest request
    ) {
        // @formatter:on
        return userService.applyVolunteer(userId, request);
    }
}