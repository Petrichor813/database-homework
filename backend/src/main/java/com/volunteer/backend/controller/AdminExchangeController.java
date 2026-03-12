package com.volunteer.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.response.AdminExchangeRecordResponse;
import com.volunteer.backend.dto.response.PageResponse;
import com.volunteer.backend.service.AdminExchangeService;

@RestController
@RequestMapping("/api/admin/exchange-record")
public class AdminExchangeController {
    private final AdminExchangeService adminExchangeService;

    public AdminExchangeController(AdminExchangeService adminExchangeService) {
        this.adminExchangeService = adminExchangeService;
    }

    @GetMapping("/get")
    public ResponseEntity<PageResponse<AdminExchangeRecordResponse>> getExchangeRecords(
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size
    ) {
        return ResponseEntity.ok(adminExchangeService.getExchangeRecords(status, page, size));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminExchangeRecordResponse> approveExchange(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        String note = request.getOrDefault("note", "管理员批准兑换");
        return ResponseEntity.ok(adminExchangeService.approveExchange(id, note));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminExchangeRecordResponse> rejectExchange(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        String note = request.getOrDefault("note", "管理员拒绝兑换");
        return ResponseEntity.ok(adminExchangeService.rejectExchange(id, note));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<AdminExchangeRecordResponse> updateExchange(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request
    ) {
        Long number = null;
        if (request.get("number") != null) {
            number = ((Number) request.get("number")).longValue();
        }
        String status = (String) request.get("status");
        return ResponseEntity.ok(adminExchangeService.updateExchange(id, number, status));
    }
}
