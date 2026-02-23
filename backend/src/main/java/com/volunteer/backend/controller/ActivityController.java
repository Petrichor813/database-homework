package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ActivityResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.service.ActivityService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // @formatter:off
    @GetMapping("/get-activities")
    public ResponseEntity<PageResponse<ActivityResponse>> getActivities(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") String type,
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(required = false) String date,
        @RequestParam(defaultValue = "time") String sort
    ) {
        // @formatter:on
        return ResponseEntity.ok(activityService.getActivities(page, size, keyword, type, status, date, sort));
    }
}
