package com.volunteer.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.SignupRecord;

public interface SignupRecordRepository extends JpaRepository<SignupRecord, Long> {
    Page<SignupRecord> findByVolunteerIdOrderBySignupTimeDesc(Long volunteerId, Pageable pageable);

    List<SignupRecord> findByActivityIdOrderBySignupTimeDesc(Long activityId);

    @Query("SELECT COALESCE(SUM(s.actualHours), 0) FROM SignupRecord s WHERE s.volunteerId = :volunteerId")
    Integer sumHoursByVolunteerId(@Param("volunteerId") Long volunteerId);
    
    boolean existsByVolunteerIdAndActivityId(Long volunteerId, Long activityId);
    
    Optional<SignupRecord> findByVolunteerIdAndActivityId(Long volunteerId, Long activityId);
}