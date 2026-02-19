package com.volunteer.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.ExchangeRecordResponse;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.ExchangeItem;
import com.volunteer.backend.entity.ExchangeRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.repository.ExchangeItemRepository;
import com.volunteer.backend.repository.ExchangeRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class ExchangeRecordService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final VolunteerRepository volunteerRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;

    // @formatter:off
    public ExchangeRecordService(
        VolunteerRepository volunteerRepository,
        ExchangeItemRepository exchangeItemRepository,
        ExchangeRecordRepository exchangeRecordRepository
    ) {
        // @formatter:on
        this.volunteerRepository = volunteerRepository;
        this.exchangeItemRepository = exchangeItemRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
    }

    public PageResponse<ExchangeRecordResponse> getExchangeRecords(Long volunteerId, int page, int size) {
        Optional<Volunteer> v = volunteerRepository.findByIdAndDeletedFalse(volunteerId);
        if (!v.isPresent()) {
            throw new IllegalArgumentException("未找到志愿者信息");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ExchangeRecord> recordPage = exchangeRecordRepository.findByVolunteerIdOrderByOrderTimeDesc(volunteerId,
                pageable);

        List<ExchangeRecordResponse> content = new ArrayList<>();
        List<ExchangeRecord> records = recordPage.getContent();

        for (int i = 0; i < records.size(); i++) {
            ExchangeRecord record = records.get(i);
            Optional<ExchangeItem> ei = exchangeItemRepository.findById(record.getItemId());
            if (!ei.isPresent()) {
                throw new IllegalArgumentException("商品不存在");
            }
            ExchangeItem item = ei.get();

            // @formatter:off
            content.add(
                new ExchangeRecordResponse(
                    record.getId(),
                    item.getName(),
                    record.getNumber(),
                    record.getTotalPoints(),
                    record.getStatus().toString(),
                    record.getOrderTime().format(DATETIME_FORMATTER),
                    record.getProcessTime() != null ? record.getProcessTime().format(DATETIME_FORMATTER) : null,
                    record.getNote(),
                    record.getRecvInfo()
                )
            );
            // @formatter:on
        }

        return new PageResponse<>(content, page, size, recordPage.getTotalElements(), recordPage.getTotalPages());
    }
}
