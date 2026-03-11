package com.volunteer.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.PointChangeRecord;
import com.volunteer.backend.enums.PointChangeType;

@Repository
public interface PointChangeRecordRepository extends JpaRepository<PointChangeRecord, Long> {
    Page<PointChangeRecord> findByVolunteerIdOrderByChangeTimeDesc(Long volunteerId, Pageable pageable);

    Page<PointChangeRecord> findAllByOrderByChangeTimeDesc(Pageable pageable);

    Page<PointChangeRecord> findByChangeTypeOrderByChangeTimeDesc(PointChangeType changeType, Pageable pageable);

    @Query("SELECT p FROM PointChangeRecord p WHERE p.volunteerId IN (SELECT v.id FROM Volunteer v WHERE v.name LIKE "
            + "CONCAT('%', :keyword, '%')) ORDER BY p.changeTime DESC")
    // @formatter:off
    Page<PointChangeRecord> findByVolunteerNameKeywordOrderByChangeTimeDesc(
        @Param("keyword") String keyword,
        Pageable pageable
    );
    // @formatter:on

    @Query("SELECT p FROM PointChangeRecord p WHERE p.changeType = :changeType AND p.volunteerId IN (SELECT v.id FROM"
            + " Volunteer v WHERE v.name LIKE CONCAT('%', :keyword, '%')) ORDER BY p.changeTime DESC")
    // @formatter:off
    Page<PointChangeRecord> findByChangeTypeAndVolunteerNameKeywordOrderByChangeTimeDesc(
        @Param("changeType") PointChangeType changeType,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    // @formatter:on
}
