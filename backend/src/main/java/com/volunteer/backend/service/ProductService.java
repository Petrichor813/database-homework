package com.volunteer.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.ExchangeRequest;
import com.volunteer.backend.dto.ExchangeResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.ProductResponse;
import com.volunteer.backend.entity.ExchangeRecord;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.enums.ProductStatus;
import com.volunteer.backend.enums.ProductType;
import com.volunteer.backend.enums.RelatedRecordType;
import com.volunteer.backend.repository.ExchangeRecordRepository;
import com.volunteer.backend.repository.PointChangeRecordRepository;
import com.volunteer.backend.repository.ProductRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final VolunteerRepository volunteerRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final PointChangeRecordRepository pointChangeRecordRepository;

    public ProductService(
        ProductRepository productRepository,
        VolunteerRepository volunteerRepository,
        ExchangeRecordRepository exchangeRecordRepository,
        PointChangeRecordRepository pointChangeRecordRepository
    ) {
        this.productRepository = productRepository;
        this.volunteerRepository = volunteerRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.pointChangeRecordRepository = pointChangeRecordRepository;
    }

    private Comparator<Product> buildComparator() {
        return new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                int weightCompare = Integer.compare(p2.getSortWeight(), p1.getSortWeight());
                if (weightCompare != 0) {
                    return weightCompare;
                }
                return p2.getCreateTime().compareTo(p1.getCreateTime());
            }
        };
    }

    private void updateProductStatus(Product product) {
        if (product.getStatus() == ProductStatus.DELETED) {
            return;
        }

        if (product.getStock() <= 0 && product.getStatus() == ProductStatus.AVAILABLE) {
            product.setStatus(ProductStatus.SOLD_OUT);
        } else if (product.getStock() > 0 && product.getStatus() == ProductStatus.SOLD_OUT) {
            product.setStatus(ProductStatus.AVAILABLE);
        }
    }

    private ProductResponse buildResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getPrice(),
            product.getStock(),
            product.getStatus(),
            product.getImageUrl()
        );
    }

    public PageResponse<ProductResponse> getProducts(
        int page,
        int size,
        String keyword,
        String category
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        List<Product> allProducts = new ArrayList<>(productRepository.findAll());

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            updateProductStatus(product);
            boolean shouldInclude = true;

            if (product.getStatus() == ProductStatus.DELETED) {
                shouldInclude = false;
            }

            if (shouldInclude && keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
                String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
                String description = product.getDescription() == null ? ""
                        : product.getDescription().toLowerCase(Locale.ROOT);
                if (!name.contains(lowerKeyword) && !description.contains(lowerKeyword)) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude && category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
                try {
                    ProductType productType = ProductType.valueOf(category);
                    if (!product.getCategory().equals(productType)) {
                        shouldInclude = false;
                    }
                } catch (IllegalArgumentException e) {
                    shouldInclude = false;
                }
            }

            if (shouldInclude) {
                filtered.add(product);
            }
        }

        Comparator<Product> comparator = buildComparator();
        filtered.sort(comparator);

        List<ProductResponse> content = new ArrayList<>();
        for (Product product : filtered) {
            content.add(buildResponse(product));
        }

        int totalElements = content.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        if (start >= totalElements) {
            return new PageResponse<>(new ArrayList<>(), page, size, totalElements, totalPages);
        }
        int end = Math.min(start + size, totalElements);
        List<ProductResponse> pagedContent = content.subList(start, end);

        return new PageResponse<>(pagedContent, page, size, totalElements, totalPages);
    }

    public ExchangeResponse exchangeProduct(Long userId, ExchangeRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        if (request.getNumber() == null || request.getNumber() <= 0) {
            throw new IllegalArgumentException("兑换数量必须大于0");
        }

        Optional<Volunteer> v = volunteerRepository.findByUserIdAndDeletedFalse(userId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("您还未申请成为志愿者或志愿者身份未认证");
        }
        Volunteer volunteer = v.get();

        if (!volunteer.isCertified()) {
            throw new IllegalArgumentException("您的志愿者身份未认证，无法兑换商品");
        }

        Optional<Product> p = productRepository.findById(request.getProductId());
        if (p.isEmpty()) {
            throw new IllegalArgumentException("商品不存在");
        }
        Product product = p.get();

        updateProductStatus(product);

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalArgumentException("该商品当前不可兑换");
        }

        if (product.getStock() < request.getNumber()) {
            throw new IllegalArgumentException("商品库存不足");
        }

        double totalPoints = product.getPrice() * request.getNumber();
        double currentPoints = getCurrentPoints(volunteer.getId());

        if (currentPoints < totalPoints) {
            throw new IllegalArgumentException("您的积分不足，当前积分为: " + currentPoints);
        }

        product.setStock(product.getStock() - request.getNumber());
        updateProductStatus(product);
        productRepository.save(product);

        ExchangeRecord exchangeRecord = new ExchangeRecord(
            volunteer.getId(),
            product.getId(),
            request.getNumber(),
            totalPoints
        );
        exchangeRecordRepository.save(exchangeRecord);

        PointChangeRecord pointChangeRecord = new PointChangeRecord(
            volunteer.getId(),
            -totalPoints,
            PointChangeType.EXCHANGE_USE,
            "兑换商品: " + product.getName(),
            exchangeRecord.getId(),
            RelatedRecordType.EXCHANGE
        );
        pointChangeRecordRepository.save(pointChangeRecord);

        return new ExchangeResponse(exchangeRecord.getId(), "兑换成功");
    }

    private double getCurrentPoints(Long volunteerId) {
        Double total = pointChangeRecordRepository.sumPointsByVolunteerId(volunteerId);
        return total != null ? total : 0.0;
    }
}
