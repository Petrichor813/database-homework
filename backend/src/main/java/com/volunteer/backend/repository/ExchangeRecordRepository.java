package com.volunteer.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.ExchangeRecord;

@Repository
public interface ExchangeRecordRepository extends JpaRepository<ExchangeRecord, Long> {
    List<ExchangeRecord> findByVolunteerId(Long volunteerId);
}
