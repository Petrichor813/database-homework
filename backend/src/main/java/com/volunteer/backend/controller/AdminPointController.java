package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.AdminPointAdjustRequest;
import com.volunteer.backend.dto.AdminPointRecordResponse;
import com.volunteer.backend.dto.AdminPointUpdateRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.service.AdminPointService;

@RestController
@RequestMapping("/api/admin/point-records")
public class AdminPointController {
    private final AdminPointService adminPointService;

    public AdminPointController(AdminPointService adminPointService) {
        this.adminPointService = adminPointService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminPointRecordResponse>> getPointRecords(
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(adminPointService.getPointRecords(type, keyword, page, size));
    }

    @PostMapping
    public ResponseEntity<AdminPointRecordResponse> addPointRecord(@RequestBody AdminPointAdjustRequest request) {
        return ResponseEntity.ok(adminPointService.addPointRecord(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminPointRecordResponse> updatePointRecord(
            @PathVariable Long id,
            @RequestBody AdminPointUpdateRequest request) {
        return ResponseEntity.ok(adminPointService.updatePointRecord(id, request));
    }

    @DeleteMapping("/{id}/revert")
    public ResponseEntity<Void> revertPointRecord(@PathVariable Long id) {
        adminPointService.revertPointRecord(id);
        return ResponseEntity.ok().build();
    }
}
