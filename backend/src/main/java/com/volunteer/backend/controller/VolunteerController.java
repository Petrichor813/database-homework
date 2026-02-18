package com.volunteer.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.PointChangeRecordResponse;
import com.volunteer.backend.service.VolunteerService;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    // @formatter:off
    @GetMapping("/{volunteerId}/point-change-records")
    public PageResponse<PointChangeRecordResponse> getPointChangeRecords(
        @PathVariable Long volunteerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return volunteerService.getPointChangeRecords(volunteerId, page, size);
    }
    // @formatter:on
}
