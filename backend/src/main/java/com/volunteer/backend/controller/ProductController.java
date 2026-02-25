package com.volunteer.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteer.backend.dto.ExchangeRequest;
import com.volunteer.backend.dto.ExchangeResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.ProductResponse;
import com.volunteer.backend.entity.User;
import com.volunteer.backend.service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // @formatter:off
    @GetMapping("/get-products")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") String category
    ) {
        // @formatter:on
        return ResponseEntity.ok(productService.getProducts(page, size, keyword, category));
    }

    // @formatter:off
    @PostMapping("/exchange")
    public ResponseEntity<ExchangeResponse> exchangeProduct(
        @AuthenticationPrincipal User user,
        @RequestBody ExchangeRequest request
    ) {
        // @formatter:on
        return ResponseEntity.ok(productService.exchangeProduct(user.getId(), request));
    }
}
