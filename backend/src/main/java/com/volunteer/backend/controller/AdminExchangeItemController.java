package com.volunteer.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.volunteer.backend.dto.AdminItemImportRequest;
import com.volunteer.backend.entity.ExchangeItem;
import com.volunteer.backend.service.AdminExchangeItemService;

@RestController
@RequestMapping("/api/admin/item")
public class AdminExchangeItemController {
    private final AdminExchangeItemService adminExchangeItemService;

    public AdminExchangeItemController(AdminExchangeItemService adminExchangeItemService) {
        this.adminExchangeItemService = adminExchangeItemService;
    }

    @PostMapping("/import")
    public ResponseEntity<ExchangeItem> importItem(@RequestBody AdminItemImportRequest request) {
        ExchangeItem item = adminExchangeItemService.importItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }
}
