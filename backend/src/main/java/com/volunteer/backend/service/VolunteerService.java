package com.volunteer.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.dto.PointChangeRecordResponse;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.repository.PointChangeRecordRepository;

@Service
public class VolunteerService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PointChangeRecordRepository pointChangeRecordRepository;

    // @formatter:off
    public VolunteerService(
        PointChangeRecordRepository pointChangeRecordRepository
    ) {
        this.pointChangeRecordRepository = pointChangeRecordRepository;
    }
    // @formatter:on

    private String getPointChangeTypeLabel(PointChangeType type) {
        switch (type) {
        case ACTIVITY_EARN:
            return "活动结算";
        case EXCHANGE_USE:
            return "兑换消耗";
        case ADMIN_ADJUST:
            return "管理员调整";
        case SYSTEM_BONUS:
            return "系统奖励";
        default:
            throw new IllegalArgumentException("未知的积分变更类型");
        }
    }

    public PageResponse<PointChangeRecordResponse> getPointChangeRecords(Long volunteerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PointChangeRecord> recordPage = pointChangeRecordRepository
                .findByVolunteerIdOrderByChangeTimeDesc(volunteerId, pageable);

        List<PointChangeRecord> recordList = recordPage.getContent();
        List<PointChangeRecordResponse> responseList = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            PointChangeRecord record = recordList.get(i);
            String formattedTime = record.getChangeTime().format(DATETIME_FORMATTER);
            // @formatter:off
            responseList.add(
                new PointChangeRecordResponse(
                    formattedTime,
                    getPointChangeTypeLabel(record.getChangeType()),
                    record.getChangePoints(),
                    record.getNote()
                )
            );
            // @formatter:on
        }

        // @formatter:off
        return new PageResponse<>(
            responseList,
            recordPage.getNumber(),
            recordPage.getSize(),
            recordPage.getTotalElements(),
            recordPage.getTotalPages()
        );
        // @formatter:on
    }
}
