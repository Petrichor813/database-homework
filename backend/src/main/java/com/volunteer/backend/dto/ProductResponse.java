package com.volunteer.backend.dto;

import com.volunteer.backend.enums.ProductStatus;
import com.volunteer.backend.enums.ProductType;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private ProductType category;
    private Double price;
    private Long stock;
    private ProductStatus status;
    private String imageUrl;

    public ProductResponse() {
    }

    public ProductResponse(
        Long id,
        String name,
        String description,
        ProductType category,
        Double price,
        Long stock,
        ProductStatus status,
        String imageUrl
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getCategory() {
        return category;
    }

    public void setCategory(ProductType category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
