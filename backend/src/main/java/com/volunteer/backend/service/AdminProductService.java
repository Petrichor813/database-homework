package com.volunteer.backend.service;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.AdminProductImportRequest;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.repository.ProductRepository;

@Service
public class AdminProductService {
    private final ProductRepository exchangeItemRepository;

    public AdminProductService(ProductRepository exchangeItemRepository) {
        this.exchangeItemRepository = exchangeItemRepository;
    }

    public Product importItem(AdminProductImportRequest request) throws IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("商品导入请求不能为空");
        }

        String name = request.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }

        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("兑换积分必须大于0");
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }

        if (request.getSortWeight() == null || request.getSortWeight() < 0) {
            throw new IllegalArgumentException("排序权重不能为负数");
        }

        if (request.getCategory() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }

        if (request.getStatus() == null) {
            throw new IllegalArgumentException("商品状态不能为空");
        }

        Product item = new Product(name.trim(), request.getPrice(), request.getStock());

        String desc = request.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = null;
        } else {
            desc = desc.trim();
        }

        item.setDescription(desc);
        item.setCategory(request.getCategory());
        item.setStatus(request.getStatus());
        item.setSortWeight(request.getSortWeight());

        String imageUrl = request.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = null;
        } else {
            imageUrl = imageUrl.trim();
        }
        item.setImageUrl(imageUrl);

        return exchangeItemRepository.save(item);
    }
}
