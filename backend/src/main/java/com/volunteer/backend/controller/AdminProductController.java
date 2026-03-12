package com.volunteer.backend.controller;

import org.springframework.http.HttpStatus;
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

import com.volunteer.backend.dto.request.AdminProductImportRequest;
import com.volunteer.backend.dto.request.AdminProductRequest;
import com.volunteer.backend.dto.response.PageResponse;
import com.volunteer.backend.dto.response.ProductResponse;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.service.AdminProductService;

@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {
    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size
    ) {
        return ResponseEntity.ok(adminProductService.getProducts(keyword, page, size));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @RequestBody AdminProductRequest request
    ) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<Product> importProduct(
        @RequestBody AdminProductImportRequest request
    ) {
        Product product = adminProductService.importProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}
