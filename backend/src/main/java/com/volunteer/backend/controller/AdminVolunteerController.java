package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.AdminVolunteerResponse;
import com.volunteer.backend.dto.AdminVolunteerReviewRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.service.AdminVolunteerService;

@RestController
@RequestMapping("/api/admin/volunteer")
public class AdminVolunteerController {
    private final AdminVolunteerService adminVolunteerService;

    public AdminVolunteerController(AdminVolunteerService adminVolunteerService) {
        this.adminVolunteerService = adminVolunteerService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminVolunteerResponse>> getVolunteers(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminVolunteerService.getVolunteers(status, page, size));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<AdminVolunteerResponse> reviewVolunteer(@PathVariable Long id,
            @RequestBody AdminVolunteerReviewRequest request) {
        return ResponseEntity.ok(adminVolunteerService.reviewVolunteer(id, request));
    }
}