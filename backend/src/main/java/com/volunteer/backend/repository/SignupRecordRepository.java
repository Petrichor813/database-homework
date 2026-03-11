package com.volunteer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.SignupRecord;
import com.volunteer.backend.enums.SignupStatus;

@Repository
public interface SignupRecordRepository extends JpaRepository<SignupRecord, Long> {
    // @formatter:off
    Page<SignupRecord> findByVolunteerIdAndStatusNotOrderBySignupTimeDesc(
        Long volunteerId,
        SignupStatus status,
        Pageable pageable
    );
    // @formatter:on

    List<SignupRecord> findByVolunteerId(Long volunteerId);

    List<SignupRecord> findByVolunteerIdAndStatusNot(Long volunteerId, SignupStatus status);

    List<SignupRecord> findByActivityIdOrderBySignupTimeDesc(Long activityId);

    @Query("SELECT COALESCE(SUM(s.actualHours), 0) FROM SignupRecord s WHERE s.volunteerId = :volunteerId")
    Integer sumHoursByVolunteerId(@Param("volunteerId") Long volunteerId);

    Optional<SignupRecord> findByVolunteerIdAndActivityId(Long volunteerId, Long activityId);

    @Query("SELECT s FROM SignupRecord s WHERE s.volunteerId = :volunteerId AND s.activityId IN :activityIds")
    // @formatter:off
    List<SignupRecord> findByVolunteerIdAndActivityIds(
        @Param("volunteerId") Long volunteerId,
        @Param("activityIds") List<Long> activityIds
    );
    // @formatter:on

    @Query("SELECT COUNT(s) > 0 FROM SignupRecord s WHERE s.volunteerId = :volunteerId AND s.activityId = :activityId"
            + " AND s.status != :status")
    // @formatter:off
    boolean existsByVolunteerIdAndActivityIdAndStatusNot(
        @Param("volunteerId") Long volunteerId,
        @Param("activityId") Long activityId,
        @Param("status") SignupStatus status
    );
    // @formatter:on
}