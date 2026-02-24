// AdminItemImportRequest.java
package com.volunteer.backend.dto;

import com.volunteer.backend.enums.ItemCategory;
import com.volunteer.backend.enums.ItemStatus;

public class AdminItemImportRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private ItemCategory category;
    private ItemStatus status;
    private Integer sortWeight;
    private String imageUrl;

    public AdminItemImportRequest() {
    }

    // @formatter:off
    public AdminItemImportRequest(
        String name,
        String description,
        Double price,
        Integer stock,
        ItemCategory category,
        ItemStatus status,
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

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
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
