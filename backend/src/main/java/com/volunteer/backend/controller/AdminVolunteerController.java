package com.volunteer.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.AdminVolunteerResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.service.AdminVolunteerService;

@RestController
@RequestMapping("/api/admin/volunteer")
public class AdminVolunteerController {
    private final AdminVolunteerService adminVolunteerService;

    public AdminVolunteerController(AdminVolunteerService adminVolunteerService) {
        this.adminVolunteerService = adminVolunteerService;
    }

    // @formatter:off
    @GetMapping
    public ResponseEntity<PageResponse<AdminVolunteerResponse>> getVolunteers(
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminVolunteerService.getVolunteers(status, page, size, keyword));
    }

    // @formatter:off
    @GetMapping("/search")
    public ResponseEntity<PageResponse<AdminVolunteerResponse>> searchVolunteers(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminVolunteerService.searchVolunteers(keyword, page, size));
    }

    // @formatter:off
    @PostMapping("/{id}/review")
    public ResponseEntity<AdminVolunteerResponse> reviewVolunteer(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        // @formatter:on
        String action = request.get("action");
        String note = request.get("note");
        return ResponseEntity.ok(adminVolunteerService.reviewVolunteer(id, action, note));
    }
}