package com.volunteer.backend.controller;

import java.util.Map;

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

import com.volunteer.backend.dto.AdminExchangeRecordResponse;
import com.volunteer.backend.dto.AdminProductRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.ProductResponse;
import com.volunteer.backend.service.AdminExchangeService;
import com.volunteer.backend.service.AdminProductService;

@RestController
@RequestMapping("/api/admin")
public class AdminExchangeController {
    private final AdminExchangeService adminExchangeService;
    private final AdminProductService adminProductService;

    // @formatter:off
    public AdminExchangeController(
        AdminExchangeService adminExchangeService,
        AdminProductService adminProductService
    ) {
        // @formatter:on
        this.adminExchangeService = adminExchangeService;
        this.adminProductService = adminProductService;
    }

    // @formatter:off
    @GetMapping("/exchange-records")
    public ResponseEntity<PageResponse<AdminExchangeRecordResponse>> getExchangeRecords(
        @RequestParam(defaultValue = "ALL") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminExchangeService.getExchangeRecords(status, page, size));
    }

    // @formatter:off
    @PostMapping("/exchange-records/{id}/approve")
    public ResponseEntity<AdminExchangeRecordResponse> approveExchange(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        // @formatter:on
        String note = request.getOrDefault("note", "管理员批准兑换");
        return ResponseEntity.ok(adminExchangeService.approveExchange(id, note));
    }

    // @formatter:off
    @PostMapping("/exchange-records/{id}/reject")
    public ResponseEntity<AdminExchangeRecordResponse> rejectExchange(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        // @formatter:on
        String note = request.getOrDefault("note", "管理员拒绝兑换");
        return ResponseEntity.ok(adminExchangeService.rejectExchange(id, note));
    }

    // @formatter:off
    @PutMapping("/exchange-records/{id}")
    public ResponseEntity<AdminExchangeRecordResponse> updateExchange(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request
    ) {
        // @formatter:on
        Long number = null;
        if (request.get("number") != null) {
            number = ((Number) request.get("number")).longValue();
        }
        String status = (String) request.get("status");
        return ResponseEntity.ok(adminExchangeService.updateExchange(id, number, status));
    }

    // @formatter:off
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminProductService.getProducts(keyword, page, size));
    }

    // @formatter:off
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
        @RequestBody AdminProductRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminProductService.createProduct(request));
    }

    // @formatter:off
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @RequestBody AdminProductRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
