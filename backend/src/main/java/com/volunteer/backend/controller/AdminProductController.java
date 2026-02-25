package com.volunteer.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.volunteer.backend.dto.AdminProductImportRequest;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.service.AdminProductService;

@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {
    private final AdminProductService adminExchangeItemService;

    public AdminProductController(AdminProductService adminExchangeItemService) {
        this.adminExchangeItemService = adminExchangeItemService;
    }

    @PostMapping("/import")
    public ResponseEntity<Product> importItem(@RequestBody AdminProductImportRequest request) {
        Product item = adminExchangeItemService.importItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }
}
