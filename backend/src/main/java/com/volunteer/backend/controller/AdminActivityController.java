package com.volunteer.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.AdminActivityImportRequest;
import com.volunteer.backend.entity.Activity;
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
}
