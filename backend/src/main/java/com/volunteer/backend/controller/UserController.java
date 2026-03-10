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

import com.volunteer.backend.dto.request.ModifyVolunteerApplicationRequest;
import com.volunteer.backend.dto.request.VolunteerApplyRequest;
import com.volunteer.backend.dto.response.UserProfileResponse;
import com.volunteer.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // @formatter:off
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
        @PathVariable Long userId
    ) {
        // @formatter:on
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    // @formatter:off
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
        @PathVariable Long userId,
        @RequestBody Map<String, String> request
    ) {
        // @formatter:on
        String username = request.get("username");
        String phone = request.get("phone");
        return ResponseEntity.ok(userService.updateProfile(userId, username, phone));
    }

    // @formatter:off
    @PostMapping("/{userId}/volunteer-apply")
    public ResponseEntity<UserProfileResponse> applyVolunteer(
        @PathVariable Long userId,
        @Valid @RequestBody VolunteerApplyRequest request
    ) {
        // @formatter:on
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.applyVolunteer(userId, request));
    }

    // @formatter:off
    @PutMapping("/{userId}/volunteer-application")
    public ResponseEntity<UserProfileResponse> updateVolunteerApplication(
        @PathVariable Long userId,
        @Valid @RequestBody ModifyVolunteerApplicationRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(userService.updateVolunteerApplication(userId, request));
    }

    // @formatter:off
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteAccount(
        @PathVariable Long userId
    ) {
        // @formatter:on
        userService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", "账号已注销"));
    }

    // @formatter:off
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
        @PathVariable Long userId,
        @RequestBody Map<String, String> request
    ) {
        // @formatter:on
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }
}