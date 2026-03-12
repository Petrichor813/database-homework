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

import com.volunteer.backend.dto.response.AdminExchangeRecordResponse;
import com.volunteer.backend.dto.response.PageResponse;
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
    
    // @formatter:off
    private AdminExchangeRecordResponse buildResponse(
        ExchangeRecord record,
        String volunteerName,
        String productName,
        Double productPrice
    ) {
        return new AdminExchangeRecordResponse(
            record.getId(),
            record.getVolunteerId(),
            volunteerName,
            productName,
            record.getNumber(),
            record.getTotalPoints(),
            productPrice,
            record.getStatus().toString(),
            record.getOrderTime().format(DATETIME_FORMATTER),
            record.getProcessTime() != null ? record.getProcessTime().format(DATETIME_FORMATTER) : null,
            record.getNote(), record.getRecvInfo()
        );
    }
    // @formatter:on

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
            Double productPrice = 0.0;
            Optional<Product> p = productRepository.findById(record.getProductId());
            if (p.isPresent()) {
                productName = p.get().getName();
                productPrice = p.get().getPrice();
            }

            content.add(buildResponse(record, volunteerName, productName, productPrice));
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
    public AdminExchangeRecordResponse approveExchange(Long recordId, String note) {
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

        record.setStatus(ExchangeStatus.PROCESSING);
        record.setProcessTime(LocalDateTime.now());
        record.setNote(note);

        ExchangeRecord saved = exchangeRecordRepository.save(record);

        String volunteerName = "";
        Optional<Volunteer> volunteer = volunteerRepository.findById(saved.getVolunteerId());
        if (volunteer.isPresent()) {
            volunteerName = volunteer.get().getName();
        }

        return buildResponse(saved, volunteerName, product.getName(), product.getPrice());
    }

    @Transactional
    public AdminExchangeRecordResponse rejectExchange(Long recordId, String note) {
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
        record.setNote(note);

        // @formatter:off
        PointChangeRecord refundRecord = new PointChangeRecord(
            record.getVolunteerId(),
            record.getTotalPoints(),
            PointChangeType.ADMIN_ADJUST,
            "兑换被拒绝，积分退还: " + note,
            record.getId(),
            RelatedRecordType.EXCHANGE
        );
        // @formatter:on
        pointChangeRecordRepository.save(refundRecord);

        // 更新志愿者积分
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(record.getVolunteerId());
        if (v.isEmpty()) {
            throw new IllegalArgumentException("志愿者账号已注销或不存在");
        }

        Volunteer volunteer = v.get();
        String volunteerName = volunteer.getName();
        volunteer.setPoints(volunteer.getPoints() + record.getTotalPoints());
        volunteerRepository.save(volunteer);

        ExchangeRecord saved = exchangeRecordRepository.save(record);

        String productName = "";
        Double productPrice = 0.0;
        Optional<Product> p = productRepository.findById(saved.getProductId());
        if (p.isPresent()) {
            productName = p.get().getName();
            productPrice = p.get().getPrice();
        }

        return buildResponse(saved, volunteerName, productName, productPrice);
    }

    @Transactional
    public AdminExchangeRecordResponse updateExchange(Long recordId, Long number, String status) {
        Optional<ExchangeRecord> er = exchangeRecordRepository.findById(recordId);
        if (er.isEmpty()) {
            throw new IllegalArgumentException("兑换记录不存在");
        }

        ExchangeRecord record = er.get();

        if (record.getStatus() != ExchangeStatus.PROCESSING && record.getStatus() != ExchangeStatus.COMPLETED) {
            throw new IllegalArgumentException("只能编辑处理中或已完成的兑换记录");
        }

        Optional<Product> p = productRepository.findById(record.getProductId());
        if (p.isEmpty()) {
            throw new IllegalArgumentException("商品不存在");
        }

        Product product = p.get();

        Long oldNumber = record.getNumber();
        Long newNumber = number;
        if (newNumber == null || newNumber <= 0) {
            throw new IllegalArgumentException("兑换数量必须大于0");
        }

        String newStatus = status;
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("兑换状态不能为空");
        }

        ExchangeStatus exchangeStatus;
        try {
            exchangeStatus = ExchangeStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的兑换状态: " + newStatus);
        }

        if (exchangeStatus != ExchangeStatus.PROCESSING && exchangeStatus != ExchangeStatus.COMPLETED) {
            throw new IllegalArgumentException("只能设置为处理中或已完成状态");
        }

        Double oldTotalPoints = record.getTotalPoints();
        Double newTotalPoints = product.getPrice() * newNumber;

        if (exchangeStatus == ExchangeStatus.PROCESSING) {
            if (product.getStock() + oldNumber < newNumber) {
                throw new IllegalArgumentException("商品库存不足，无法更新兑换数量");
            }
            product.setStock(product.getStock() + oldNumber - newNumber);
        }

        ExchangeStatus oldStatus = record.getStatus();

        record.setNumber(newNumber);
        record.setTotalPoints(newTotalPoints);
        record.setStatus(exchangeStatus);

        if (exchangeStatus == ExchangeStatus.COMPLETED && oldStatus != ExchangeStatus.COMPLETED) {
            record.setProcessTime(LocalDateTime.now());
        }

        ExchangeRecord saved = exchangeRecordRepository.save(record);
        productRepository.save(product);

        if (!oldTotalPoints.equals(newTotalPoints)) {
            Double pointDiff = newTotalPoints - oldTotalPoints;

            Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(record.getVolunteerId());
            if (v.isEmpty()) {
                throw new IllegalArgumentException("志愿者账号已注销或不存在");
            }

            Volunteer volunteer = v.get();
            volunteer.setPoints(volunteer.getPoints() - pointDiff);
            volunteerRepository.save(volunteer);

            PointChangeRecord adjustRecord = new PointChangeRecord(record.getVolunteerId(), pointDiff,
                    PointChangeType.ADMIN_ADJUST, "兑换记录编辑，积分调整: " + (pointDiff > 0 ? "增加" : "减少") + Math.abs(pointDiff),
                    record.getId(), RelatedRecordType.EXCHANGE);
            pointChangeRecordRepository.save(adjustRecord);
        }

        String volunteerName = "";
        Optional<Volunteer> volunteer = volunteerRepository.findByIdAndDeletedFalse(saved.getVolunteerId());
        if (volunteer.isPresent()) {
            volunteerName = volunteer.get().getName();
        }

        return buildResponse(saved, volunteerName, product.getName(), product.getPrice());
    }
}
