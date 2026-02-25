package com.volunteer.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ModifyVolunteerApplicationRequest;
import com.volunteer.backend.dto.UpdateUserProfileRequest;
import com.volunteer.backend.dto.UserProfileResponse;
import com.volunteer.backend.dto.VolunteerApplyRequest;
import com.volunteer.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    // @formatter:off
    public ResponseEntity<UserProfileResponse> updateProfile(
        @PathVariable Long userId,
        @RequestBody UpdateUserProfileRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/{userId}/volunteer-apply")
    // @formatter:off
    public ResponseEntity<UserProfileResponse> applyVolunteer(
        @PathVariable Long userId,
        @Valid @RequestBody VolunteerApplyRequest request
    ) {
        // @formatter:on
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.applyVolunteer(userId, request));
    }

    @PutMapping("/{userId}/volunteer-application")
    // @formatter:off
    public ResponseEntity<UserProfileResponse> updateVolunteerApplication(
        @PathVariable Long userId,
        @Valid @RequestBody ModifyVolunteerApplicationRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(userService.updateVolunteerApplication(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable Long userId) {
        userService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", "账号已注销"));
    }
}