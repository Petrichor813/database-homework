package com.volunteer.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.ExchangeRecord;

@Repository
public interface ExchangeRecordRepository extends JpaRepository<ExchangeRecord, Long> {
    Page<ExchangeRecord> findByVolunteerIdOrderByOrderTimeDesc(Long volunteerId, Pageable pageable);
}
