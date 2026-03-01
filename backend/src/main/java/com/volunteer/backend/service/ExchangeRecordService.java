package com.volunteer.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.ExchangeRecordResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.entity.Product;
import com.volunteer.backend.entity.ExchangeRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.ExchangeStatus;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.enums.RelatedRecordType;
import com.volunteer.backend.repository.ProductRepository;
import com.volunteer.backend.repository.ExchangeRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;
import com.volunteer.backend.repository.PointChangeRecordRepository;

@Service
public class ExchangeRecordService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VolunteerRepository volunteerRepository;
    private final ProductRepository productRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final PointChangeRecordRepository pointChangeRecordRepository;

    // @formatter:off
    public ExchangeRecordService(
        VolunteerRepository volunteerRepository,
        ProductRepository productRepository,
        ExchangeRecordRepository exchangeRecordRepository,
        PointChangeRecordRepository pointChangeRecordRepository
    ) {
        // @formatter:on
        this.volunteerRepository = volunteerRepository;
        this.productRepository = productRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.pointChangeRecordRepository = pointChangeRecordRepository;
    }

    public PageResponse<ExchangeRecordResponse> getExchangeRecords(Long volunteerId, int page, int size) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("未找到志愿者信息");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ExchangeRecord> recordPage = exchangeRecordRepository.findByVolunteerIdOrderByOrderTimeDesc(volunteerId,
                pageable);

        List<ExchangeRecordResponse> content = new ArrayList<>();
        List<ExchangeRecord> records = recordPage.getContent();

        for (ExchangeRecord r : records) {
            Optional<Product> product = productRepository.findById(r.getProductId());
            if (product.isEmpty()) {
                throw new IllegalArgumentException("商品不存在");
            }

            // @formatter:off
            content.add(
                new ExchangeRecordResponse(
                    r.getId(),
                    product.get().getName(),
                    r.getNumber(),
                    r.getTotalPoints(),
                    r.getStatus().toString(),
                    r.getOrderTime().format(DATETIME_FORMATTER),
                    r.getProcessTime() != null ? r.getProcessTime().format(DATETIME_FORMATTER) : null,
                    r.getNote(),
                    r.getRecvInfo()
                )
            );
            // @formatter:on
        }

        return new PageResponse<>(content, page, size, recordPage.getTotalElements(), recordPage.getTotalPages());
    }

    @Transactional
    public void cancelExchangeRecord(Long volunteerId, Long recordId) {
        // 验证志愿者是否存在
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (v.isEmpty()) {
            throw new IllegalArgumentException("未找到志愿者信息");
        }

        // 验证兑换记录是否存在，并且属于该志愿者
        Optional<ExchangeRecord> recordOptional = exchangeRecordRepository.findById(recordId);
        if (recordOptional.isEmpty()) {
            throw new IllegalArgumentException("未找到兑换记录");
        }

        ExchangeRecord record = recordOptional.get();
        if (!record.getVolunteerId().equals(volunteerId)) {
            throw new IllegalArgumentException("无权操作此兑换记录");
        }

        // 验证兑换记录是否可以取消
        if (!record.isCancellable()) {
            throw new IllegalArgumentException("该兑换记录不可取消");
        }

        // 取消兑换记录
        record.setStatus(ExchangeStatus.CANCELLED);
        record.setProcessTime(LocalDateTime.now());
        record.setNote("用户主动取消兑换");
        exchangeRecordRepository.save(record);

        // 退还积分
        PointChangeRecord refundRecord = new PointChangeRecord(
            volunteerId,
            record.getTotalPoints(),
            PointChangeType.ADMIN_ADJUST,
            "取消兑换，积分退还: " + record.getNote(),
            record.getId(),
            RelatedRecordType.EXCHANGE
        );
        pointChangeRecordRepository.save(refundRecord);
    }
}
