package com.volunteer.backend.controller;

import java.util.List;

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

import com.volunteer.backend.dto.AdminActivityImportRequest;
import com.volunteer.backend.dto.AdminActivityUpdateRequest;
import com.volunteer.backend.dto.AdminSignupRecordResponse;
import com.volunteer.backend.dto.AdminSignupUpdateRequest;
import com.volunteer.backend.entity.Activity;
import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.service.AdminActivityService;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {
    private final AdminActivityService adminActivityService;

    public AdminActivityController(AdminActivityService adminActivityService) {
        this.adminActivityService = adminActivityService;
    }

    // @formatter:off
    @PostMapping("/import")
    public ResponseEntity<Activity> importActivity(
        @RequestBody AdminActivityImportRequest request
    ) {
        // @formatter:on
        Activity activity = adminActivityService.importActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    // @formatter:off
    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> updateActivity(
        @PathVariable Long activityId,
        @RequestBody AdminActivityUpdateRequest request
    ) {
        // @formatter:on
        Activity activity = adminActivityService.updateActivity(activityId, request);
        return ResponseEntity.ok(activity);
    }

    // @formatter:off
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
        @PathVariable Long activityId
    ) {
        // @formatter:on
        adminActivityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    // @formatter:off
    @GetMapping("/{activityId}/signups")
    public ResponseEntity<List<AdminSignupRecordResponse>> getActivitySignups(
        @PathVariable Long activityId
    ) {
        // @formatter:on
        List<AdminSignupRecordResponse> signups = adminActivityService.getActivitySignups(activityId);
        return ResponseEntity.ok(signups);
    }

    // @formatter:off
    @PutMapping("/{activityId}/signups/{signupId}")
    public ResponseEntity<SignupRecord> updateSignupRecord(
        @PathVariable Long activityId,
        @PathVariable Long signupId,
        @RequestBody AdminSignupUpdateRequest request
    ) {
        // @formatter:on
        SignupRecord record = adminActivityService.updateSignupRecord(activityId, signupId, request);
        return ResponseEntity.ok(record);
    }
}
