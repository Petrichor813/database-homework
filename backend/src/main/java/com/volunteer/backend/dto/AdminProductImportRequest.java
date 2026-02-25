package com.volunteer.backend.dto;

import com.volunteer.backend.enums.ProductType;
import com.volunteer.backend.enums.ProductStatus;

public class AdminProductImportRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private ProductType category;
    private ProductStatus status;
    private Integer sortWeight;
    private String imageUrl;

    public AdminProductImportRequest() {
    }

    // @formatter:off
    public AdminProductImportRequest(
        String name,
        String description,
        Double price,
        Integer stock,
        ProductType category,
        ProductStatus status,
        Integer sortWeight,
        String imageUrl
    ) {
        // @formatter:on
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
        this.sortWeight = sortWeight;
        this.imageUrl = imageUrl;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public ProductType getCategory() {
        return category;
    }

    public void setCategory(ProductType category) {
        this.category = category;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Integer getSortWeight() {
        return sortWeight;
    }

    public void setSortWeight(Integer sortWeight) {
        this.sortWeight = sortWeight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
