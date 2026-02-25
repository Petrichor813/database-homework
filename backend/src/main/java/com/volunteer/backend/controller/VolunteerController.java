package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ExchangeRecordResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.PointChangeRecordResponse;
import com.volunteer.backend.dto.SignupRecordResponse;
import com.volunteer.backend.service.ExchangeRecordService;
import com.volunteer.backend.service.SignupRecordService;
import com.volunteer.backend.service.VolunteerService;

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {
    private final VolunteerService volunteerService;
    private final SignupRecordService signupRecordService;
    private final ExchangeRecordService exchangeRecordService;

    // @formatter:off
    public VolunteerController(
        VolunteerService volunteerService,
        SignupRecordService signupRecordService,
        ExchangeRecordService exchangeRecordService
    ) {
        // @formatter:on
        this.volunteerService = volunteerService;
        this.signupRecordService = signupRecordService;
        this.exchangeRecordService = exchangeRecordService;
    }

    // @formatter:off
    @GetMapping("/{volunteerId}/point-change-records")
    public ResponseEntity<PageResponse<PointChangeRecordResponse>> getPointChangeRecords(
        @PathVariable Long volunteerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(volunteerService.getPointChangeRecords(volunteerId, page, size));
    }

    // @formatter:off
    @GetMapping("/{volunteerId}/signup-records")
    public ResponseEntity<PageResponse<SignupRecordResponse>> getSignupRecords(
        @PathVariable Long volunteerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(signupRecordService.getSignupRecords(volunteerId, page, size));
    }

    // @formatter:off
    @GetMapping("/{volunteerId}/exchange-records")
    public ResponseEntity<PageResponse<ExchangeRecordResponse>> getExchangeRecords(
        @PathVariable Long volunteerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(exchangeRecordService.getExchangeRecords(volunteerId, page, size));
    }

    // @formatter:off
    @DeleteMapping("/{volunteerId}/exchange-records/{recordId}")
    public ResponseEntity<Void> cancelExchangeRecord(
        @PathVariable Long volunteerId,
        @PathVariable Long recordId
    ) {
        // @formatter:on
        exchangeRecordService.cancelExchangeRecord(volunteerId, recordId);
        return ResponseEntity.noContent().build();
    }
}