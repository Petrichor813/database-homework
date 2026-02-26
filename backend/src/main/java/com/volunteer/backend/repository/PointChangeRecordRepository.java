package com.volunteer.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.enums.PointChangeType;

public interface PointChangeRecordRepository extends JpaRepository<PointChangeRecord, Long> {
    List<PointChangeRecord> findByVolunteerId(Long volunteerId);

    Page<PointChangeRecord> findByVolunteerIdOrderByChangeTimeDesc(Long volunteerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.changePoints), 0) FROM PointChangeRecord p WHERE p.volunteerId = :volunteerId")
    Double sumPointsByVolunteerId(@Param("volunteerId") Long volunteerId);

    Page<PointChangeRecord> findAllByOrderByChangeTimeDesc(Pageable pageable);

    Page<PointChangeRecord> findByChangeTypeOrderByChangeTimeDesc(PointChangeType changeType, Pageable pageable);

    @Query("SELECT p FROM PointChangeRecord p WHERE p.volunteerId IN (SELECT v.id FROM Volunteer v WHERE v.name LIKE %:keyword%) ORDER BY p.changeTime DESC")
    Page<PointChangeRecord> findByVolunteerNameKeywordOrderByChangeTimeDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM PointChangeRecord p WHERE p.changeType = :changeType AND p.volunteerId IN (SELECT v.id FROM Volunteer v WHERE v.name LIKE %:keyword%) ORDER BY p.changeTime DESC")
    Page<PointChangeRecord> findByChangeTypeAndVolunteerNameKeywordOrderByChangeTimeDesc(@Param("changeType") PointChangeType changeType, @Param("keyword") String keyword, Pageable pageable);
}
