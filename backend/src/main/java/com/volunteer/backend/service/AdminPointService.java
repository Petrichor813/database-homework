package com.volunteer.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.volunteer.backend.dto.AdminPointAdjustRequest;
import com.volunteer.backend.dto.AdminPointRecordResponse;
import com.volunteer.backend.dto.AdminPointUpdateRequest;
import com.volunteer.backend.dto.PageResponse;
import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.PointChangeType;
import com.volunteer.backend.repository.PointChangeRecordRepository;
import com.volunteer.backend.repository.VolunteerRepository;

@Service
public class AdminPointService {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PointChangeRecordRepository pointChangeRecordRepository;
    private final VolunteerRepository volunteerRepository;

    // @formatter:off
    public AdminPointService(
        PointChangeRecordRepository pointChangeRecordRepository,
        VolunteerRepository volunteerRepository
    ) {
        // @formatter:on
        this.pointChangeRecordRepository = pointChangeRecordRepository;
        this.volunteerRepository = volunteerRepository;
    }

    private AdminPointRecordResponse buildResponse(PointChangeRecord record, String volunteerName) {
        // @formatter:off
        return new AdminPointRecordResponse(
            record.getId(),
            record.getVolunteerId(),
            volunteerName,
            record.getChangeType(),
            record.getChangePoints(),
            record.getBalanceAfter(),
            record.getReason(),
            record.getNote(),
            record.getRelatedRecordType(),
            record.getRelatedRecordId(),
            record.getChangeTime().format(DATETIME_FORMATTER)
        );
        // @formatter:on
    }

    public PageResponse<AdminPointRecordResponse> getPointRecords(String type, String keyword, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页记录数必须大于0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PointChangeRecord> recordPage;

        String trimmedKeyword = (keyword != null) ? keyword.trim() : "";

        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            if (trimmedKeyword.isEmpty()) {
                recordPage = pointChangeRecordRepository.findAllByOrderByChangeTimeDesc(pageable);
            } else {
                recordPage = pointChangeRecordRepository.findByVolunteerNameKeywordOrderByChangeTimeDesc(trimmedKeyword,
                        pageable);
            }
        } else {
            try {
                PointChangeType changeType = PointChangeType.valueOf(type.toUpperCase());
                if (trimmedKeyword.isEmpty()) {
                    recordPage = pointChangeRecordRepository.findByChangeTypeOrderByChangeTimeDesc(changeType,
                            pageable);
                } else {
                    recordPage = pointChangeRecordRepository
                            .findByChangeTypeAndVolunteerNameKeywordOrderByChangeTimeDesc(changeType, trimmedKeyword,
                                    pageable);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("未知的积分变动类型: " + type);
            }
        }

        List<AdminPointRecordResponse> content = new ArrayList<>();
        for (PointChangeRecord record : recordPage.getContent()) {
            String volunteerName = "";
            Optional<Volunteer> volunteer = volunteerRepository.findById(record.getVolunteerId());
            if (volunteer.isPresent()) {
                volunteerName = volunteer.get().getName();
            }
            content.add(buildResponse(record, volunteerName));
        }

        return new PageResponse<>(content, recordPage.getNumber(), recordPage.getSize(), recordPage.getTotalElements(),
                recordPage.getTotalPages());
    }

    @Transactional
    public AdminPointRecordResponse addPointRecord(AdminPointAdjustRequest request) {
        if (request.getVolunteerId() == null) {
            throw new IllegalArgumentException("志愿者ID不能为空");
        }

        Optional<Volunteer> volunteerOpt = volunteerRepository.findById(request.getVolunteerId());
        if (volunteerOpt.isEmpty()) {
            throw new IllegalArgumentException("志愿者不存在");
        }

        Volunteer volunteer = volunteerOpt.get();

        if (request.getChangePoints() == null) {
            throw new IllegalArgumentException("变动数量不能为空");
        }

        if (request.getChangeType() == null) {
            throw new IllegalArgumentException("变动类型不能为空");
        }

        String reason = (request.getReason() != null) ? request.getReason().trim() : "";
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("变动原因不能为空");
        }

        if (reason.length() > 200) {
            throw new IllegalArgumentException("变动原因长度不能超过200个字符");
        }

        String note = (request.getNote() != null) ? request.getNote().trim() : "";
        if (note.length() > 200) {
            throw new IllegalArgumentException("备注长度不能超过200个字符");
        }

        Double currentBalance = pointChangeRecordRepository.sumPointsByVolunteerId(request.getVolunteerId());
        Double newBalance = currentBalance + request.getChangePoints();

        PointChangeRecord record = new PointChangeRecord(request.getVolunteerId(), request.getChangePoints(),
                request.getChangeType(), reason, null, null);
        record.setNote(note);
        record.setBalanceAfter(newBalance);

        PointChangeRecord saved = pointChangeRecordRepository.save(record);

        return buildResponse(saved, volunteer.getName());
    }

    @Transactional
    public AdminPointRecordResponse updatePointRecord(Long recordId, AdminPointUpdateRequest request) {
        Optional<PointChangeRecord> recordOpt = pointChangeRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("积分记录不存在");
        }

        PointChangeRecord record = recordOpt.get();

        if (request.getChangePoints() != null) {
            if (request.getChangePoints().equals(record.getChangePoints())) {
                throw new IllegalArgumentException("变动数量未发生变化");
            }

            Double oldChangePoints = record.getChangePoints();
            Double newChangePoints = request.getChangePoints();
            Double difference = newChangePoints - oldChangePoints;

            Double currentBalance = pointChangeRecordRepository.sumPointsByVolunteerId(record.getVolunteerId());
            Double newBalance = currentBalance + difference;

            record.setChangePoints(newChangePoints);
            record.setBalanceAfter(newBalance);
        }

        if (request.getReason() != null) {
            String reason = request.getReason().trim();
            if (reason.isEmpty()) {
                throw new IllegalArgumentException("变动原因不能为空");
            }
            if (reason.length() > 200) {
                throw new IllegalArgumentException("变动原因长度不能超过200个字符");
            }
            record.setReason(reason);
        }

        if (request.getNote() != null) {
            String note = request.getNote().trim();
            if (note.length() > 200) {
                throw new IllegalArgumentException("备注长度不能超过200个字符");
            }
            record.setNote(note);
        }

        PointChangeRecord saved = pointChangeRecordRepository.save(record);

        String volunteerName = "";
        Optional<Volunteer> volunteer = volunteerRepository.findById(saved.getVolunteerId());
        if (volunteer.isPresent()) {
            volunteerName = volunteer.get().getName();
        }

        return buildResponse(saved, volunteerName);
    }

    @Transactional
    public void revertPointRecord(Long recordId) {
        Optional<PointChangeRecord> recordOpt = pointChangeRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("积分记录不存在");
        }

        PointChangeRecord originalRecord = recordOpt.get();

        Double revertAmount = -originalRecord.getChangePoints();

        String reason = "撤销积分记录 ID: " + recordId;
        String note = "原记录: " + originalRecord.getReason();

        Double currentBalance = pointChangeRecordRepository.sumPointsByVolunteerId(originalRecord.getVolunteerId());
        Double newBalance = currentBalance + revertAmount;

        PointChangeRecord revertRecord = new PointChangeRecord(originalRecord.getVolunteerId(), revertAmount,
                PointChangeType.ADMIN_ADJUST, reason, originalRecord.getId(), null);
        revertRecord.setNote(note);
        revertRecord.setBalanceAfter(newBalance);

        pointChangeRecordRepository.save(revertRecord);
    }
}
