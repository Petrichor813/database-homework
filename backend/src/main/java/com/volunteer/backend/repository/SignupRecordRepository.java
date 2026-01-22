package com.volunteer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.SignupRecord;

public interface SignupRecordRepository extends JpaRepository<SignupRecord, Long> {
    @Query("SELECT COALESCE(SUM(s.actualHours), 0) FROM SignupRecord s WHERE s.volunteerId = :volunteerId")
    Integer sumHoursByVolunteerId(@Param("volunteerId") Long volunteerId);
}
