package com.volunteer.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.PointChangeRecord;

public interface PointChangeRecordRepository extends JpaRepository<PointChangeRecord, Long> {
    List<PointChangeRecord> findByVolunteerId(Long volunteerId);

    List<PointChangeRecord> findTop5ByVolunteerIdOrderByChangeTimeDesc(Long volunteerId);

    @Query("SELECT COALESCE(SUM(p.changePoints), 0) FROM PointsChangeRecord p WHERE p.volunteerId = :volunteerId")
    Double sumPointsByVolunteerId(@Param("volunteerId") Long volunteerId);
}
