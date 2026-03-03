package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.AdminExchangeRecordResponse;
import com.volunteer.backend.dto.AdminExchangeProcessRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.ExchangeRecord;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ExchangeStatus;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.enums.RelatedRecordType;
import com.volunteer.backend.repository.ExchangeRecordRepository;
import com.volunteer.backend.repository.PointChangeRecordRepository;
import com.volunteer.backend.repository.ProductRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class AdminExchangeService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ExchangeRecordRepository exchangeRecordRepository;
    private final VolunteerRepository volunteerRepository;
    private final ProductRepository productRepository;
    private final PointChangeRecordRepository pointChangeRecordRepository;

    // @formatter:off
    public AdminExchangeService(
        ExchangeRecordRepository exchangeRecordRepository,
        VolunteerRepository volunteerRepository,
        ProductRepository productRepository,
        PointChangeRecordRepository pointChangeRecordRepository
    ) {
            // @formatter:on
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.volunteerRepository = volunteerRepository;
        this.productRepository = productRepository;
        this.pointChangeRecordRepository = pointChangeRecordRepository;
    }

    private AdminExchangeRecordResponse buildResponse(ExchangeRecord record, String volunteerName, String productName) {
        // @formatter:off
        return new AdminExchangeRecordResponse(
            record.getId(),
            record.getVolunteerId(),
            volunteerName,
            productName,
            record.getNumber(),
            record.getTotalPoints(),
            record.getStatus().toString(),
            record.getOrderTime().format(DATETIME_FORMATTER),
            record.getProcessTime() != null ? record.getProcessTime().format(DATETIME_FORMATTER) : null,
            record.getNote(), record.getRecvInfo()
        );
        // @formatter:on
    }

    public PageResponse<AdminExchangeRecordResponse> getExchangeRecords(String status, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderTime"));
        Page<ExchangeRecord> recordPage;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            recordPage = exchangeRecordRepository.findAll(pageable);
        } else {
            try {
                ExchangeStatus exchangeStatus = ExchangeStatus.valueOf(status.toUpperCase());
                recordPage = exchangeRecordRepository.findByStatus(exchangeStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("未知的兑换状态: " + status);
            }
        }

        List<AdminExchangeRecordResponse> content = new ArrayList<>();
        for (ExchangeRecord record : recordPage.getContent()) {
            String volunteerName = "";
            Optional<Volunteer> volunteer = volunteerRepository.findById(record.getVolunteerId());
            if (volunteer.isPresent()) {
                volunteerName = volunteer.get().getName();
            }

            String productName = "";
            Optional<Product> p = productRepository.findById(record.getProductId());
            if (p.isPresent()) {
                productName = p.get().getName();
            }

            content.add(buildResponse(record, volunteerName, productName));
        }

        // @formatter:off
        return new PageResponse<>(
            content,
            recordPage.getNumber(),
            recordPage.getSize(),
            recordPage.getTotalElements(),
            recordPage.getTotalPages()
        );
        // @formatter:on
    }

    @Transactional
    public AdminExchangeRecordResponse approveExchange(Long recordId, AdminExchangeProcessRequest request) {
        Optional<ExchangeRecord> er = exchangeRecordRepository.findById(recordId);
        if (er.isEmpty()) {
            throw new IllegalArgumentException("兑换记录不存在");
        }

        ExchangeRecord record = er.get();

        if (record.getStatus() != ExchangeStatus.REVIEWING) {
            throw new IllegalArgumentException("该兑换记录已被处理，无法重复操作");
        }

        Optional<Product> p = productRepository.findById(record.getProductId());
        if (p.isEmpty()) {
            throw new IllegalArgumentException("商品不存在");
        }

        Product product = p.get();

        if (product.getStock() < record.getNumber()) {
            throw new IllegalArgumentException("商品库存不足，无法批准兑换");
        }

        product.setStock(product.getStock() - record.getNumber());
        productRepository.save(product);

        record.setStatus(ExchangeStatus.COMPLETED);
        record.setProcessTime(LocalDateTime.now());
        record.setNote(request.getNote() != null ? request.getNote().trim() : "管理员批准兑换");

        ExchangeRecord saved = exchangeRecordRepository.save(record);

        String volunteerName = "";
        Optional<Volunteer> volunteer = volunteerRepository.findById(saved.getVolunteerId());
        if (volunteer.isPresent()) {
            volunteerName = volunteer.get().getName();
        }

        return buildResponse(saved, volunteerName, product.getName());
    }

    @Transactional
    public AdminExchangeRecordResponse rejectExchange(Long recordId, AdminExchangeProcessRequest request) {
        Optional<ExchangeRecord> er = exchangeRecordRepository.findById(recordId);
        if (er.isEmpty()) {
            throw new IllegalArgumentException("兑换记录不存在");
        }

        ExchangeRecord record = er.get();

        if (record.getStatus() != ExchangeStatus.REVIEWING) {
            throw new IllegalArgumentException("该兑换记录已被处理，无法重复操作");
        }

        record.setStatus(ExchangeStatus.REJECTED);
        record.setProcessTime(LocalDateTime.now());
        record.setNote(request.getNote() != null ? request.getNote().trim() : "管理员拒绝兑换");

        // @formatter:off
        PointChangeRecord refundRecord = new PointChangeRecord(
            record.getVolunteerId(),
            record.getTotalPoints(),
            PointChangeType.ADMIN_ADJUST,
            "兑换被拒绝，积分退还: " + (request.getNote() != null ? request.getNote() : "管理员拒绝兑换"),
            record.getId(),
            RelatedRecordType.EXCHANGE
        );
        // @formatter:on
        pointChangeRecordRepository.save(refundRecord);

        // 更新志愿者积分
        Optional<Volunteer> v = volunteerRepository.findById(record.getVolunteerId());
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者不存在");
        }

        Volunteer volunteer = v.get();
        String volunteerName = volunteer.getName();
        volunteer.setPoints(volunteer.getPoints() + record.getTotalPoints());
        volunteerRepository.save(volunteer);

        ExchangeRecord saved = exchangeRecordRepository.save(record);

        String productName = "";
        Optional<Product> p = productRepository.findById(saved.getProductId());
        if (p.isPresent()) {
            productName = p.get().getName();
        }

        return buildResponse(saved, volunteerName, productName);
    }
}
