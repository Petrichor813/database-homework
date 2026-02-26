package com.volunteer.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.volunteer.backend.entity.Volunteer;
import com.volunteer.backend.enums.VolunteerStatus;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Page<Volunteer> findByDeletedFalse(Pageable pageable);

    Optional<Volunteer> findByIdAndDeletedFalse(Long id);

    Optional<Volunteer> findByUserIdAndDeletedFalse(Long userId);

    Page<Volunteer> findByStatusAndDeletedFalse(VolunteerStatus status, Pageable pageable);

    Page<Volunteer> findByStatusNotAndDeletedFalse(VolunteerStatus status, Pageable pageable);

    @Query("SELECT v FROM Volunteer v WHERE v.deleted = false AND v.name LIKE %:keyword%")
    Page<Volunteer> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}