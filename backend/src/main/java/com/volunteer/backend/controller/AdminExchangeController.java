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

import com.volunteer.backend.dto.AdminExchangeRecordResponse;
import com.volunteer.backend.dto.AdminExchangeProcessRequest;
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

    public AdminExchangeController(AdminExchangeService adminExchangeService, AdminProductService adminProductService) {
        this.adminExchangeService = adminExchangeService;
        this.adminProductService = adminProductService;
    }

    @GetMapping("/exchange-records")
    public ResponseEntity<PageResponse<AdminExchangeRecordResponse>> getExchangeRecords(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(adminExchangeService.getExchangeRecords(status, page, size));
    }

    @PostMapping("/exchange-records/{id}/approve")
    public ResponseEntity<AdminExchangeRecordResponse> approveExchange(
            @PathVariable Long id,
            @RequestBody AdminExchangeProcessRequest request) {
        return ResponseEntity.ok(adminExchangeService.approveExchange(id, request));
    }

    @PostMapping("/exchange-records/{id}/reject")
    public ResponseEntity<AdminExchangeRecordResponse> rejectExchange(
            @PathVariable Long id,
            @RequestBody AdminExchangeProcessRequest request) {
        return ResponseEntity.ok(adminExchangeService.rejectExchange(id, request));
    }

    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(adminProductService.getProducts(keyword, page, size));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(adminProductService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
