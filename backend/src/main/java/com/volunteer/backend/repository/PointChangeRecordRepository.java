package com.volunteer.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.PointChangeRecord;

public interface PointChangeRecordRepository extends JpaRepository<PointChangeRecord, Long> {
    List<PointChangeRecord> findByVolunteerId(Long volunteerId);

    Page<PointChangeRecord> findByVolunteerIdOrderByChangeTimeDesc(Long volunteerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.changePoints), 0) FROM PointChangeRecord p WHERE p.volunteerId = :volunteerId")
    Double sumPointsByVolunteerId(@Param("volunteerId") Long volunteerId);
}
