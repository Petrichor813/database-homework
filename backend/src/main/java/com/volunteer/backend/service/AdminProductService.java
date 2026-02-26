package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.AdminProductRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.ProductResponse;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.enums.ProductStatus;
import com.volunteer.backend.repository.ProductRepository;

@Service
public class AdminProductService {
    private final ProductRepository productRepository;

    public AdminProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductResponse buildResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getCategory(),
                product.getPrice(), product.getStock(), product.getStatus(), product.getImageUrl());
    }

    public PageResponse<ProductResponse> getProducts(String keyword, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Product> productPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            productPage = productRepository.findByStatusNot(ProductStatus.DELETED, pageable);
        } else {
            String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            productPage = productRepository.findByStatusNotAndNameContainingIgnoreCase(ProductStatus.DELETED,
                    lowerKeyword, pageable);
        }

        List<ProductResponse> content = new ArrayList<>();
        for (Product product : productPage.getContent()) {
            content.add(buildResponse(product));
        }

        return new PageResponse<>(content, productPage.getNumber(), productPage.getSize(),
                productPage.getTotalElements(), productPage.getTotalPages());
    }

    @Transactional
    public ProductResponse createProduct(AdminProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (request.getName().trim().length() > 50) {
            throw new IllegalArgumentException("商品名称长度不能超过50个字符");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("库存数量不能为负数");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("商品状态不能为空");
        }

        Product product = new Product(request.getName().trim(), request.getPrice(), request.getStock());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setCategory(request.getCategory());
        product.setStatus(request.getStatus());
        product.setSortWeight(request.getSortWeight() != null ? request.getSortWeight() : 0);
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        product.setCreateTime(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return buildResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, AdminProductRequest request) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("商品不存在");
        }

        Product product = productOpt.get();

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalArgumentException("该商品已被删除，无法修改");
        }

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("商品名称不能为空");
            }
            if (request.getName().trim().length() > 50) {
                throw new IllegalArgumentException("商品名称长度不能超过50个字符");
            }
            product.setName(request.getName().trim());
        }

        if (request.getPrice() != null) {
            if (request.getPrice() <= 0) {
                throw new IllegalArgumentException("商品价格必须大于0");
            }
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new IllegalArgumentException("库存数量不能为负数");
            }
            product.setStock(request.getStock());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }

        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getSortWeight() != null) {
            product.setSortWeight(request.getSortWeight());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl().trim());
        }

        product.setUpdateTime(LocalDateTime.now());

        Product saved = productRepository.save(product);
        return buildResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("商品不存在");
        }

        Product product = productOpt.get();

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalArgumentException("该商品已被删除");
        }

        product.setStatus(ProductStatus.DELETED);
        product.setUpdateTime(LocalDateTime.now());
        productRepository.save(product);
    }

    public Product importProduct(com.volunteer.backend.dto.AdminProductImportRequest request)
            throws IllegalArgumentException {
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

        Product product = new Product(name.trim(), request.getPrice(), request.getStock());

        String desc = request.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = null;
        } else {
            desc = desc.trim();
        }

        product.setDescription(desc);
        product.setCategory(request.getCategory());
        product.setStatus(request.getStatus());
        product.setSortWeight(request.getSortWeight());

        String imageUrl = request.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = null;
        } else {
            imageUrl = imageUrl.trim();
        }
        product.setImageUrl(imageUrl);

        return productRepository.save(product);
    }
}
